package us.monoid.psql.async;

import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.Data;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;

import us.monoid.psql.async.callback.PromisedResult;
import us.monoid.psql.async.callback.ResultListener;
import us.monoid.psql.async.message.AuthenticationRequest;
import us.monoid.psql.async.message.BackendKeyData;
import us.monoid.psql.async.message.CommandComplete;
import us.monoid.psql.async.message.DataRow;
import us.monoid.psql.async.message.ErrorResponse;
import us.monoid.psql.async.message.ParameterStatus;
import us.monoid.psql.async.message.Password;
import us.monoid.psql.async.message.Query;
import us.monoid.psql.async.message.ReadyForQuery;
import us.monoid.psql.async.message.RowDescription;
import us.monoid.psql.async.message.Startup;

/**
 * A currently running transaction. In essence, a single connection to the Database which can be re-used as needed. Instances of this class are managed by the Postgres class
 * 
 * @author beders
 * 
 */
public class Transaction {
	Postgres pg;

	Map<String, String> params = new HashMap<>();

	NetSocket socket;

	enum Phase {
		startup, ready, executing, queryResults
	}

	Phase phase = Phase.startup;

	Handler<Transaction> connectionHandler;
	Handler<Transaction> executionHandler;
	ResultListener resultListener;
	

	private int processID; // process ID of the back-end
	private int secretKey; // secret used in cancel requests to the back-end

	private String lastResult;

	private Row currentRow;    // current data transmission (columns, rows)

	private int rowCounter;

	Transaction(Postgres aPostgresDB) {
		pg = aPostgresDB;
	}

	void connect(Handler<Transaction> aConnectionHandler) {
		connectionHandler = aConnectionHandler;
		NetClient client = pg.vertx.createNetClient();
		client.connect(pg.port, pg.host, new AsyncResultHandler<NetSocket>() {
			@Override
			public void handle(AsyncResult<NetSocket> event) {
				if (event.succeeded()) {
					socket = event.result();
					System.out.println(socket.toString());
					socket.dataHandler(new Handler<Buffer>() {
						@Override
						public void handle(Buffer buffer) {
							parseMessages(buffer);
						}
					});
					socket.write(new Startup(new Buffer(20)).write(pg.user, pg.db));
				} else {
					connectionHandler.handle(null);
				}
			}
		});
	}

	/** Receive messages and dispatch accordingly */
	protected void parseMessages(Buffer buffer) {
		for (int i = 0, len = buffer.length(), msgSize = 0; i < len; i += msgSize) {
			msgSize = buffer.getInt(i + 1) + 1;
			dispatch(buffer.getBuffer(i, i + msgSize)); // dispatch single message, the 1 is for the message type byte
		}
	}

	protected void dispatch(Buffer buffer) {
		debugMessage(buffer);
		pg.dispatch(buffer, this);
	}

	void on(CommandComplete commandComplete) {
		if (executionHandler != null) {
			this.lastResult = commandComplete.getTag();
			phase = Phase.ready;
			executionHandler.handle(this);
		}
	}

	/** Start up phase. Figuring out how to authenticate */
	void on(AuthenticationRequest ar) {
		if (ar.isAuthenticationOk()) {
			this.lastResult = "AUTHENTICATED";
			connectionHandler.handle(this);
		} else if (ar.isAuthenticationSupported()) {
			if (ar.needCleartextPassword()) {
				socket.write(new Password(new Buffer(20)).write(pg.password)); // send PasswordMessage --> onCheckPasswordMessage
			} // TODO needMD5Password
		} else {
			this.lastResult = "Unsupported Authentication - bug beders to add it";
			connectionHandler.handle(this);
			socket.close();
		}
	}

	void on(BackendKeyData backendKeyData) {
		processID = backendKeyData.getProcessID();
		secretKey = backendKeyData.getSecretKey();
	}

	void on(ParameterStatus parameterStatus) {
		parameterStatus.addToMap(params);
	}

	void on(ReadyForQuery readyForQuery) {
		if (phase == Phase.startup) {
			phase = Phase.ready;
			System.out.println("Params:" + params);
			this.lastResult = "OK";
			connectionHandler.handle(this);
		}
		if (phase == Phase.queryResults) {
			phase = Phase.ready;
			if (resultListener != null) {
				resultListener.end(rowCounter, this);
				rowCounter = 0;
			}
		}
	}

	/** Query result is about to be received, store the columns away to be made available with each result row */
	void on(RowDescription rowDescription) {
		currentRow = new Row(rowDescription.readColumns());
		if (resultListener != null) {
			resultListener.start(currentRow.columns, this);
		}
		rowCounter = 0;
		phase = phase.queryResults;
	}

	/** Received a single data row for the current column */
	void on(DataRow dataRow) {	
		if (resultListener != null) {
			currentRow.setRow(dataRow);
			resultListener.row(currentRow, this);
			rowCounter++;
		} // TODO else record result in lastResult as a String
	}
	
	private void debugMessage(Buffer buffer) {
		System.out.print("(" + (char) buffer.getByte(0) + ") ");
		System.out.print("#" + buffer.getInt(1) + ' ');
		for (int i = 5, len = buffer.length(); i < len; i++) {
			byte b = buffer.getByte(i);
			if (b < 32) {
				System.out.print(Byte.toString(b) + " ");
			} else if (b > 128) {
				System.out.print('?');
			} else {
				System.out.print((char) b);
			}
		}
		System.out.println(" Len:" + buffer.length());
	}

	void on(ErrorResponse errorResponse) {
		this.lastResult = "ERROR" + errorResponse;
		connectionHandler.handle(this);
		if (phase == Phase.startup)
			socket.close(); // no recovery during startup
	}

	public void execute(String sqlString, Handler<Transaction> result) {
		if (phase != Phase.ready) {
			throw new IllegalStateException("Connection not ready to execute a command");
		}
		executionHandler = result;
		phase = Phase.executing;
		socket.write(new Query(new Buffer(sqlString.length() + 6)).write(sqlString));
	}

	public PromisedResult execute(String string) {
		PromisedResult p = new PromisedResult();
		execute(string, p);
		return p;
	}
	
	public void query(String queryString, ResultListener result) {
		resultListener = result;
		execute(queryString, null);
	}
	
	public boolean isReady() {
		return phase == Phase.ready;
	}

	/**
	 * Check if the result of the last simple operation is toCompare
	 * 
	 * @param toCompare
	 *          the result to compare.
	 * @return true if result is identical to toCompare or a NullPointerException if you pass in null
	 */
	public boolean lastResultIs(String toCompare) {
		return toCompare == null ? lastResult == null : toCompare.equals(lastResult);
	}

	public String lastResult() {
		return lastResult;
	}

	

}