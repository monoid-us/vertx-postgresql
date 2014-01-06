package us.monoid.psql.async.callback;

import us.monoid.psql.async.Row;
import us.monoid.psql.async.Transaction;

/** Callback used when data is available */
public interface ResultRow {

	/** Callback to deliver a row of data. Row also has a reference to the column model. Use this to store or pass on query results.
	 * Note that this method will not get called for queries that return 0 rows (duh!)
	 * @param row reference to the byte buffer that contains the data.
	 * @param trx the currently run transaction
	 */
	public abstract void row(Row row, Transaction trx);

}