package us.monoid.psql.async.callback;

import us.monoid.psql.async.Columns;
import us.monoid.psql.async.Row;
import us.monoid.psql.async.Transaction;

/** Implement this interface (or use one of the existing implementations) to read the results of your query */
public interface ResultListener {
	
	/** Query was successful and results are incoming.
	 * Use this to prepare for the upcoming data by examining the column names and types */
	void start(Columns cols, Transaction trx);	
	
	/** Callback to deliver a row of data. Row also has a reference to the column model. Use this to store or pass on query results.
	 * Note that this method will not get called for queries that return 0 rows (duh!)
	 * @param row reference to the byte buffer that contains the data.
	 * @param trx the currently run transaction
	 */
	void row(Row row, Transaction trx);
	
	/** Query has finished. count contains the number of rows returned */
	void end(int count, Transaction trx);

}
