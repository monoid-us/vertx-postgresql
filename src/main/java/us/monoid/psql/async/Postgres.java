package us.monoid.psql.async;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;

import us.monoid.psql.async.message.AuthenticationRequest;
import us.monoid.psql.async.message.BackendKeyData;
import us.monoid.psql.async.message.CommandComplete;
import us.monoid.psql.async.message.DataRow;
import us.monoid.psql.async.message.ErrorResponse;
import us.monoid.psql.async.message.ParameterStatus;
import us.monoid.psql.async.message.ReadyForQuery;
import us.monoid.psql.async.message.RowDescription;

/**
 * Main class to access Postgres DB. Create an instance of this class in your verticle and store it in a field.
 * 
 * Talking to the database is done in the context of a transaction. 
 * Call withTransaction(...) to get access to a transaction reserved for this invocation of the verticle.
 * Transactions in essence are connections to the DB using NetClient.
 * Note that a single verticle can potentially create an unlimited number of connections.
 * Example: if you use the event bus to receive messages, multiple messages can be received by the verticle, which 
 * in turn can lead to multiple transactions being run at the same time (all running in the same thread).
 * 
 * 
 * Instance of the Postgres class have an internal pool of available transactions.
 * 
 * 
 * @todo - todo: limit #connections  - withTransaction should deal with a limit on the number of 'concurrent' transactions running.
 */
public class Postgres {
	static final Logger log = Logger.getLogger(Postgres.class.getName());

	Vertx vertx;

	String user;
	char[] password;
	String db;
	String host;
	int port = 5432;

	Queue<Transaction> created;
	Queue<Transaction> available;

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

	
	/** Creates a new transaction to use for the provided handler. 
	 * NOTE: no actual DB transaction is started yet. You need to use the BEGIN; SQL command to start one
	 * @param handler the handler receiving the transaction
	 */
	public void withTransaction(final Handler<Transaction> handler) {
		Transaction trx = available.poll();
		if (trx == null) { // create a new transaction
			trx = new Transaction(this);
			trx.connect(new Handler<Transaction>() {
				@Override
				public void handle(Transaction newTrx) {
					if (newTrx.lastResultIs("OK")) {
						created.add(newTrx);
						handler.handle(newTrx);
					}
				}
			});
		}
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
	default:
		System.out.println("Unknown message" + (char) buffer.getByte(0));
	}
}

}
