package us.monoid.psql.async.callback;

import us.monoid.psql.async.Transaction;
import us.monoid.psql.async.promise.Promise;

public abstract class TrxPromise extends Promise.Callback<Transaction, Transaction> {
	@Override
	public Promise<Transaction> onFulfilled(Transaction value) {
		return handle(value);
	}
	
	public abstract Promise<Transaction> handle(Transaction trx);

}
