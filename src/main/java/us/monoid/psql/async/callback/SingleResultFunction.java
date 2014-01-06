package us.monoid.psql.async.callback;

import us.monoid.psql.async.Transaction;

/** Functional interface to be used when a query result is just a single row with a single column.
 * 
 * @author beders
 * @see us.monoid.psql.async.Transaction#query(String, SingleResultFunction)
 * @param <T> the expected type of the result. Note: If in doubt when using numbers, expect Long to be returned
 */
public interface SingleResultFunction<T> {
	public abstract void result(T result, Transaction trx);
}