package us.monoid.psql.async.callback;

import us.monoid.psql.async.Columns;
import us.monoid.psql.async.Row;
import us.monoid.psql.async.Transaction;

/** Simple adapter class that calls back result(...) with a single result from the first column.
 * If you have a simple query that returns one row and one column, use this ResultListener implementation
 * Use T as the expected return type */
public abstract class SingleResult<T> implements ResultListener {
	T result;
	
	@Override
	public void start(Columns cols, Transaction trx) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void row(Row row, Transaction trx) {
		result = (T) row.asObject(0);
	}

	@Override
	public void end(int count, Transaction trx) {
		result(result, trx);
	}

	public abstract void result(T result, Transaction trx);
	
}
