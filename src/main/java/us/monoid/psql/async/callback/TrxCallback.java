package us.monoid.psql.async.callback;

import us.monoid.psql.async.Transaction;
import us.monoid.psql.async.promise.Promise;

public abstract class TrxCallback extends Promise.DoneCallback<Transaction> {
	
	@Override
	public void onFulfilled(Transaction trx) {
		handle(trx);
	}

	public abstract void handle(Transaction trx);
}
