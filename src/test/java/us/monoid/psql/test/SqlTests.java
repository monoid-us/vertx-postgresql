package us.monoid.psql.test;

import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import us.monoid.psql.async.Columns;
import us.monoid.psql.async.Postgres;
import us.monoid.psql.async.Row;
import us.monoid.psql.async.Transaction;
import us.monoid.psql.async.callback.ResultListener;
import us.monoid.psql.async.callback.SingleResult;
import us.monoid.psql.async.callback.TrxCallback;

public class SqlTests extends TestVerticle {
	private static final int maxRows = 20000;
	String owner = "'JB'", target = "'LG'";
	int amount = 50;
	
	@Test
	public void testLargeResults() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		p.withTransaction(new Handler<Transaction>() {
			@Override	public void handle(Transaction trx) {
				trx.execute("CREATE TABLE test ( name character varying (40), amount integer);", new Handler<Transaction>() {
					@Override
					public void handle(Transaction trx) {
						loadTest(trx, 0);
					}					
				});						
			}			
		});
	}
	
	protected void loadTest(Transaction trx, final int i) {
		if (i == maxRows) {
			getResult(trx);
			return;
		}
		trx.execute("INSERT INTO test ( name, amount ) VALUES ( 't" + i + "', " + i + ");").done(new TrxCallback() {			
			@Override
			public void handle(Transaction trx) {
				System.out.println("i:"+i);
				loadTest(trx, i+1);
			}
		});
		
	}

	private void getResult(Transaction trx) {
		trx.query("SELECT count(*) from test", new SingleResult<Long>() {
			@Override
			public void result(Long result, Transaction trx) {
				System.out.println("Count on test:" + result);
				readAll(trx);
			}});
	}
	

	private void readAll(Transaction trx) {
		trx.query("SELECT * from test", new ResultListener() {
			
			@Override
			public void end(int count, Transaction trx) {
				trx.execute("DROP TABLE test").done(trx.release);
				testComplete();
			}
			
			@Override
			public void row(Row row, Transaction trx) {
				System.out.println(row);
			}
			
			@Override
			public void start(Columns cols, Transaction trx) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	

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
				if (currentAmount > amount) {
					updateAccounts(trx);
				} else {
					System.out.println("Not enough money!");
					trx.execute("ROLLBACK", new Handler<Transaction>() {
						@Override	public void handle(Transaction trx) {
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
