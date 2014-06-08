package us.monoid.psql.async;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;

import com.sun.corba.se.impl.encoding.CodeSetConversion.BTCConverter;

import us.monoid.psql.async.auth.MD5Digest;
import us.monoid.psql.async.callback.PromisedResult;
import us.monoid.psql.async.callback.ResultEnd;
import us.monoid.psql.async.callback.SingleResultFunction;
import us.monoid.psql.async.callback.ResultListener;
import us.monoid.psql.async.callback.ResultRow;
import us.monoid.psql.async.callback.ResultStart;
import us.monoid.psql.async.callback.SingleResult;
import us.monoid.psql.async.message.AuthenticationRequest;
import us.monoid.psql.async.message.BackendKeyData;
import us.monoid.psql.async.message.CommandComplete;
import us.monoid.psql.async.message.DataRow;
import us.monoid.psql.async.message.EmptyQueryResponse;
import us.monoid.psql.async.message.ErrorResponse;
import us.monoid.psql.async.message.ParameterStatus;
import us.monoid.psql.async.message.ParseComplete;
import us.monoid.psql.async.message.Password;
import us.monoid.psql.async.message.Query;
import us.monoid.psql.async.message.ReadyForQuery;
import us.monoid.psql.async.message.RowDescription;
import us.monoid.psql.async.message.Startup;
import us.monoid.psql.async.promise.FulfillablePromise;
import us.monoid.psql.async.promise.Promise;
import us.monoid.psql.async.promise.Promise.DoneCallback;

/**
 * A currently running transaction. In essence, a single connection to the Database which can be re-used as needed. Instances of this class are managed by the Postgres class
 * 
 * @author beders
 * 
 */
public class Transaction {
	static final Logger log = Logger.getLogger(Transaction.class.getName());

	Postgres pg;
	MessageParser mp = new MessageParser(this);
	Map<String, String> params = new HashMap<>();

	NetSocket socket;

	enum Phase {
		startup, ready, executing, queryResults, released, extended;
	}

	Phase phase = Phase.startup;

	Handler<Transaction> connectionHandler;
	Handler<Transaction> executionHandler;
	ResultStart resultStartListener;
	ResultEnd resultEndListener;
	ResultRow resultRowListener;

	private int processID; // process ID of the back-end
	private int secretKey; // secret used in cancel requests to the back-end

	private String lastResult;

	private Row currentRow; // current data transmission (columns, rows)

	private int rowCounter;

	public final DoneCallback<? super Transaction> release = new DoneCallback<Transaction>() {
		@Override
		public void onFulfilled(Transaction trx) {
			trx.release();
		}
	};

	Transaction(Postgres aPostgresDB) {
		pg = aPostgresDB;
	}

	void connect(Handler<Transaction> aConnectionHandler) {
		connectionHandler = aConnectionHandler;
		NetClient client = pg.vertx.createNetClient();
		// client.setReceiveBufferSize(1024);
		client.connect(pg.port, pg.host, new AsyncResultHandler<NetSocket>() {
			@Override
			public void handle(AsyncResult<NetSocket> event) {
				if (event.succeeded()) {
					socket = event.result();
					socket.dataHandler(new Handler<Buffer>() {
						@Override
						public void handle(Buffer buffer) {
							mp.parseMessages(buffer,0,buffer.length());
						}
					});
					socket.write(new Startup(new Buffer(20)).write(pg.user, pg.db, pg.applicationName + "-" + this.hashCode()));
				} else {
					connectionHandler.handle(null);
				}
			}
		});
	}


	protected void dispatch(Buffer buffer) {
		if (log.isLoggable(Level.FINEST)) log.finest(debugMessage(buffer));
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
			} else if (ar.needMD5Password()) {
				//System.out.println("MD5 requested");
				socket.write(new Password(new Buffer(20)).write(MD5Digest.encode(pg.user, pg.password, ar.salt())));
			}
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
			log.info("Params:" + params);
			this.lastResult = "OK";
			connectionHandler.handle(this);
		}
		if (phase == Phase.queryResults) {
			phase = Phase.ready;
			if (resultEndListener != null) {
				resultEndListener.end(rowCounter, this);
				rowCounter = 0;
			}
		}
	}

	/** Query result is about to be received, store the columns away to be made available with each result row */
	void on(RowDescription rowDescription) {
		currentRow = new Row(rowDescription.readColumns());
		if (resultStartListener != null) {
			resultStartListener.start(currentRow.columns, this);
		}
		rowCounter = 0;
		phase = Phase.queryResults;
	}

	/** Received a single data row for the current column */
	void on(DataRow dataRow) {
		if (resultRowListener != null) {
			currentRow.setRow(dataRow);
			resultRowListener.row(currentRow, this);
			rowCounter++;
		} // TODO else record result in lastResult as a String
	}

	public void on(ParseComplete parseComplete) {
		// TODO throw error when phase not in extended
		assert phase == Phase.extended;
		
	}

	public void on(EmptyQueryResponse emptyQueryResponse) {
		emptyQueryResponse.read();
		phase = Phase.ready;
		executionHandler.handle(this);
	}

	private String debugMessage(Buffer buffer) {
		StringBuilder sb = new StringBuilder();
		sb.append("(" + (char) buffer.getByte(0) + ") ");
		sb.append("#" + buffer.getInt(1) + ' ');
		for (int i = 5, len = buffer.length(); i < len; i++) {
			byte b = buffer.getByte(i);
			if (b < 32) {
				sb.append(Byte.toString(b) + " ");
			} else if (b > 128) {
				sb.append('?');
			} else {
				sb.append((char) b);
			}
		}
		sb.append("\n Len:" + buffer.length() + "\n");
		return sb.toString();
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

	/**
	 * Run one (or more) SQL queries and specify the result listener for the results. Note that once the result listener is set, it will be used for subsequent SQL commands if those happen to return
	 * results
	 * 
	 * @param queryString
	 *          the SQL query (or several ones separated by ; )
	 * @param result
	 *          the result listener
	 */
	public void query(String queryString, ResultListener result) {
		query(queryString, result, result, result);
	}

	/**
	 * Run one (or more) SQL queries and specify the result listeners for the results. This allows you to specify different listeners for each phase of the query. One or all of the listeners can be
	 * null. Note that once the result listener is set, it will be used for subsequent SQL commands if those happen to return results.
	 * 
	 * @param queryString
	 *          the SQL query (or several ones separated by ; )
	 * @param startListener
	 *          - callback for a query start, can be null
	 * @param rowListener
	 *          - callback for row results, can be null
	 * @param endListener
	 *          - callback that indicates end of a query, can be null
	 */
	public void query(String queryString, ResultStart startListener, ResultRow rowListener, ResultEnd endListener) {
		resultStartListener = startListener;
		resultRowListener = rowListener;
		resultEndListener = endListener;
		execute(queryString, null);
	}

	/**
	 * Run one (or more) SQL queries and specify the result listeners for the results. This allows you to specify different listeners for each phase of the query. One or all of the listeners can be
	 * null. Note that once the result listener is set, it will be used for subsequent SQL commands if those happen to return results.
	 * 
	 * @param queryString
	 *          the SQL query (or several ones separated by ; )
	 * @param rowListener
	 *          - callback for row results, can be null
	 * @param endListener
	 *          - callback that indicates end of a query, can be null
	 */
	public void query(String queryString, ResultRow rowListener, ResultEnd endListener) {
		query(queryString, null, rowListener, null);
	}

	/**
	 * Run one SQL query that has a single result row and a single column.
	 * The provided function will be called with the result when the query has been received.
	 * 
	 * Notable difference is that the state of the transaction will be 'ready', so the transaction can be released for example.
	 * @param queryString
	 *          the SQL query (or several ones separated by ; )
	 * @param resultFunction - the function called with a single result.
	 */
	public <T> void query(String queryString, final SingleResultFunction<T> resultFunction) {
		query(queryString, new SingleResult<T>() {
			@Override
			public void result(T result, Transaction trx) {
				resultFunction.result(result, trx);
			}
		});
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

	/**
	 * Release the transaction and return it to the pool of available transactions. Don't use this object after you called release. Instead retrieve a new transaction using
	 * {@link us.monoid.psql.async.Postgres#withTransaction Postgres.withTransaction} If you call this while query results are still coming in, the results will be ignored and your callbacks will not be
	 * called
	 * 
	 */
	public void release() {
		resultStartListener = null;
		resultRowListener = null;
		resultEndListener = null;
		executionHandler = null;
		phase = Phase.released;
		pg.release(this);
	}

	/**
	 * Activate a transaction/connection after it was released in the transaction pool. TODO - check current phase TODO - reconnect if necessary
	 * 
	 * @param handler
	 *          the client-side handler to call with this object
	 */
	void activate(Handler<Transaction> handler) {
		assert phase == Phase.released;
		phase = Phase.ready;
		handler.handle(this);
	}

	/** Shutting down this transaction by closing the underlying connection */
	Promise<Void> close() {
		final FulfillablePromise<Void> promise = FulfillablePromise.<Void> create();
		if (socket != null) {
			socket.closeHandler(new Handler<Void>() {
				@Override
				public void handle(Void event) {
					promise.fulfill(null);
				}
			}).close();
		} else {
			promise.fulfill(null);
		}
		return promise;
	}
}
