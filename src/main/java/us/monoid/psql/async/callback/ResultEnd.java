package us.monoid.psql.async.callback;

import us.monoid.psql.async.Transaction;

/** Callback used when query has ended */
public interface ResultEnd {

	/** Query has finished. count contains the number of rows returned */
	public abstract void end(int count, Transaction trx);

}