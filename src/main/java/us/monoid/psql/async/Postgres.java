package us.monoid.psql.async;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import us.monoid.psql.async.message.AuthenticationRequest;
import us.monoid.psql.async.message.BackendKeyData;
import us.monoid.psql.async.message.CommandComplete;
import us.monoid.psql.async.message.DataRow;
import us.monoid.psql.async.message.EmptyQueryResponse;
import us.monoid.psql.async.message.ErrorResponse;
import us.monoid.psql.async.message.ParameterStatus;
import us.monoid.psql.async.message.ParseComplete;
import us.monoid.psql.async.message.ReadyForQuery;
import us.monoid.psql.async.message.RowDescription;
import us.monoid.psql.async.promise.Promise;
import us.monoid.psql.async.promise.Promise.DoneCallback;
import us.monoid.psql.async.promise.Promises;

/**
 * Main class to access a PostgreSQL DB. Create an instance of this class in your verticle and store it in a field.
 * 
 * Talking to the database is done in the context of a transaction. 
 * Call withTransaction(...) to get access to a transaction. 
 * 
 * Transactions in essence are connections to the DB using NetClient.
 * 
 * Note that a single verticle can potentially create an unlimited number of connections.
 * Example: if you use the event bus to receive messages, multiple messages can be received by the verticle, which 
 * in turn can lead to multiple transactions being run at the same time (all running in the same thread).
 * 
 * Make sure you set maxConnections (defaults to 5) to a reasonable number.
 * If calls to withTransaction can't be satisfied right away, the request (your callback) will be parked in memory.
 * Note that this could lead to out of memory conditions. 
 * 
 * 
 */
public class Postgres {
	public static final int MAX_CONNECTIONS_PER_VERTICLE = 5;

	static final Logger log = Logger.getLogger(Postgres.class.getName());

	Vertx vertx;

	String user;
	char[] password;
	String db;
	String host;
	int port = 5432;

	enum State { ready, closing };
	
	State currentState = State.ready;
	Queue<Transaction> created;  // contains all Transaction objects created
	Queue<Transaction> available; // contains Transactions available for new requests
	Queue<Handler<Transaction>> pending; // pending requests for a transaction
	
	int maxConnections = MAX_CONNECTIONS_PER_VERTICLE; // this will go wrong quickly. make sure you configure a reasonable amount depending on your load

	// message parsers - using a flyweight pattern here. Since all transactions managed by this instance run from the same thread
	// it is safe to re-use these instances for parsing
	AuthenticationRequest authenticationRequest = new AuthenticationRequest();
	BackendKeyData backendKeyData = new BackendKeyData();
	CommandComplete commandComplete = new CommandComplete();
	ErrorResponse errorResponse = new ErrorResponse();
	ParameterStatus parameterStatus = new ParameterStatus();
	ReadyForQuery readyForQuery = new ReadyForQuery();
	RowDescription rowDescription = new RowDescription();
	DataRow dataRow = new DataRow();
	ParseComplete parseComplete = new ParseComplete();
	EmptyQueryResponse emptyQueryResponse = new EmptyQueryResponse();

	String applicationName = "vertx-postgres";
	
	public Postgres(Vertx aVertx, String aUser, char[] aPassword, String aDB) {
		this(aVertx, aUser, aPassword, aDB, "127.0.0.1", 5432);
	}
	
	public Postgres(Vertx aVertx, String aUser, char[] aPassword, String aDB, String aHost) {
		this(aVertx, aUser, aPassword, aDB, aHost, 5432);
	}

	public Postgres(Vertx aVertx, String aUser, char[] aPassword, String aDB, String aHost, int aPort) {
		vertx = aVertx;
		available = new LinkedList<>();
		created = new LinkedList<>();
		
		user = aUser;
		password = aPassword;
		db = aDB;
		host = aHost;
		port = aPort;
	}

	/** Get application name used to identify connections from this instance of Postgres */
	public String applicationName() {
		return applicationName;
	}
	
	/** Set the application name used by the DB connections.
	 * Each connection will have this set as a prefix along with the hashCode of the Transaction object.
	 * You can see the final name by doing a {@code select * from pg_stat_activity;} on the DB. 
	 * Defaults to 'vertex-postgres */
	public Postgres applicationName(String aName) {
		applicationName = aName;
		return this;		
	}
	
	/** Creates a new transaction to use for the provided handler. 
	 * NOTE: no actual DB transaction is started yet. You need to use the BEGIN; SQL command to start one
	 * @param handler the handler receiving the transaction
	 */
	public void withTransaction(final Handler<Transaction> handler) {
		if (currentState == State.closing) {
			throw new IllegalStateException("Instance is closing all connections and is not ready to create new transactions");
		}
		if (log.isLoggable(Level.FINE)) {
			log.fine("Postgres driver \n" +
					"Available connections:" + available.size() + "\n" +
					"Created connections:" + created.size() + "\n" +
					"Parked trx handlers:" + (pending != null ? pending.size() : 0)
					);
		}
		Transaction trx = available.poll();
		if (trx == null) { // uh oh, no transactions available
			if (created.size() >= maxConnections) { // uh oh - too many connections already, we need to park the handler
				park(handler);
			} else {
				create(handler); // create a new transaction - hooray! we can serve out the connection right away
			}
		} else {
			trx.activate(handler); // re-activate a transaction object
		}
	}
	
	/** No currently available transactions, we need to park the request in memory until someone releases a transaction */
	protected void park(Handler<Transaction> handler) {
		if (pending == null) {
			pending = new LinkedList<>();
		}
		pending.add(handler);
	}

	protected void create(final Handler<Transaction> handler) {
		Transaction trx = new Transaction(this);
		created.add(trx);
		trx.connect(new Handler<Transaction>() {
			@Override
			public void handle(Transaction newTrx) { // TODO add proper error handling
				if (newTrx != null && newTrx.lastResultIs("OK")) {
					handler.handle(newTrx);
				} // else TODO error handling
			}
		});
	}

	/** Called from the Transaction to parse messages from the back-end */
	void dispatch(Buffer buffer, Transaction trx) {
	switch (buffer.getByte(0)) {
	case 'E':
		errorResponse.setBuffer(buffer);
		trx.on(errorResponse);
		break;
	case 'R':
		authenticationRequest.setBuffer(buffer);
		trx.on(authenticationRequest);
		break;
	case 'S':
		parameterStatus.setBuffer(buffer);
		trx.on(parameterStatus);
		break;
	case 'K':
		backendKeyData.setBuffer(buffer);
		trx.on(backendKeyData);
		break;
	case 'Z':
		readyForQuery.setBuffer(buffer);
		trx.on(readyForQuery);
		break;
	case 'C':
		commandComplete.setBuffer(buffer);
		trx.on(commandComplete);
		break;
	case 'T':
		rowDescription.setBuffer(buffer);
		trx.on(rowDescription);
		break;
	case 'D':
		dataRow.setBuffer(buffer);
		trx.on(dataRow);
		break;
	case '1':
		parseComplete.setBuffer(buffer);
		trx.on(parseComplete);
		break;
	case'I':
		emptyQueryResponse.setBuffer(buffer);
		trx.on(emptyQueryResponse);
		break;
	default:
		log.warning("Unknown message" + (char) buffer.getByte(0));
	}
}

	/** Release transaction back into pool again. Client-code should use Transaction.release() instead */
	void release(Transaction transaction) {
		Handler<Transaction> handler = pending != null ? pending.poll() : null;
		if (handler != null && currentState == State.ready) { // some poor handler finally got a transaction to work with
			transaction.activate(handler);
		} else {
			available.add(transaction);
		}
	}
	
	public int maxConnections() {
		return maxConnections;
	}
	
	/** Set the maximum number of connections this instance of the driver should make to postgres.
	 * If you lower this value later on, don't expect this to be handled yet (TODO).
	 * Best practice: set this once on creation time of a Postgres instance 
	 * @param aValue the maximum number of connections to use
	 * @return this
	 */
	public Postgres maxConnections(int aValue) {
		maxConnections = aValue;
		return this;
	}
	
	/** Close all connections/transactions. This clears out the connection pool as well.
	 * Note that it is undefined what happens to currently running queries. Some data might still be returned.
	 * Use this method only, if your verticle is in a well-defined state.
	 * 
	 * I.e. if you use closeAll, wait until the callback returns. After that you are free to use withTransaction again
	 * @param callback - will be called with "OK" if all connections were successfully closed */
	public void closeAll(final Handler<String> callback) {
		List<Promise<Void>> promises = new ArrayList<>();
		for (Transaction trx : created) {
			promises.add(trx.close());
		}
		Promises.wait(promises).done(new DoneCallback<List<Void>>() {
			@Override
			public void onFulfilled(List<Void> value) {
				// TODO add some kind of result value
				created.clear();
				available.clear();
				currentState = State.ready;
				callback.handle("OK");
			}
		});
		
	}
	

}
