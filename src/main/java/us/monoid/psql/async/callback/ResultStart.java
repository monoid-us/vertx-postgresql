package us.monoid.psql.async.callback;

import us.monoid.psql.async.Columns;
import us.monoid.psql.async.Transaction;

/** Callback used when query results are ready to be received */
public interface ResultStart {

	/** Query was successful and results are incoming.
	 * Use this to prepare for the upcoming data by examining the column names and types */
	public abstract void start(Columns cols, Transaction trx);

}