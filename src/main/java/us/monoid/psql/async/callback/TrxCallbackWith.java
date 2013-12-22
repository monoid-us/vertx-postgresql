package us.monoid.psql.async.callback;

import us.monoid.psql.async.Transaction;
import us.monoid.psql.async.promise.FulfillablePromise;
import us.monoid.psql.async.promise.Promise;

public abstract class TrxCallbackWith<R> extends Promise.Callback<Transaction, R>{

	@Override
	public Promise<R> onFulfilled(Transaction value) {
		R result = handle(value);
		FulfillablePromise<R> promise = FulfillablePromise.create();
		promise.fulfill(result);		
		return promise;
	}
	
	abstract public R handle(Transaction value);

}
