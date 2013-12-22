package us.monoid.psql.test;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import us.monoid.psql.async.Postgres;
import us.monoid.psql.async.SingleResult;
import us.monoid.psql.async.Transaction;
import us.monoid.psql.async.callback.TrxCallback;
import static org.vertx.testtools.VertxAssert.*;

public class SqlTests extends TestVerticle {
	String owner = "'JB'", target = "'LG'";
	int amount = 50;
	

	@Test
	public void testAccount() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		
		p.withTransaction(new Handler<Transaction>() {
			@Override
			public void handle(Transaction trx) {
				trx.execute("BEGIN; " +
										"CREATE TABLE account ( owner character varying (40), amount integer );" +
										"INSERT INTO account (owner, amount) VALUES ('JB', 100), ('LG', 100);").
				done(new TrxCallback() {
					@Override	public void handle(Transaction trx) {
						checkAmount(trx);
					}
				});
			}
		});
	}
	
	private void checkAmount(Transaction trx) {
		trx.query("SELECT amount FROM account WHERE owner = " + owner, new SingleResult<Integer>() {
			@Override
			public void result(Integer currentAmount, Transaction trx) {
				if (currentAmount < amount) {
					updateAccounts(trx);
				} else {
					System.out.println("Not enough money!");
					trx.execute("ROLLBACK", new Handler<Transaction>() {
						@Override	public void handle(Transaction event) {
							testComplete();
						}						
					});
				}
			}
		});
	}

	private void updateAccounts(Transaction trx) {
		trx.execute("UPDATE account SET amount = amount - " + amount +  " WHERE owner = " + owner + ";" +
								"UPDATE account SET amount = amount + " + amount + " WHERE owner = " + target + ";").
								done(new TrxCallback() {
									@Override
									public void handle(Transaction trx) {
										trx.execute("COMMIT; DROP TABLE account", new Handler<Transaction>() {
											@Override
											public void handle(Transaction event) {
												testComplete();
											}
										});
									}
								});
	}
	
}
