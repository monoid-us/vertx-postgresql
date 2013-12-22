package us.monoid.psql.async.callback;

import org.vertx.java.core.Handler;

import us.monoid.psql.async.Transaction;
import us.monoid.psql.async.promise.FulfillablePromise;

/** Promise of a transaction, can be used to chain method calls.
 * TODO - example use
 * 
 * @author beders
 *
 */
public class PromisedResult extends FulfillablePromise<Transaction> implements Handler<Transaction> {
	
	/** Call back method which delegates to the ResultHandler set up in the then method. */
	@Override
	public void handle(Transaction value) {
		fulfill(value);
	}
}
