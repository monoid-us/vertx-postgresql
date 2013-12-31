package us.monoid.psql.test;

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
	
	/** Create a table with all supported data types and test if the converters work for the returned values */
	@Test
	public void dataTypeTest() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		p.withTransaction(new Handler<Transaction>() {			
			@Override
			public void handle(Transaction trx) {
				trx.execute("CREATE TEMP TABLE datatypes ( _20 bigint, _1043 character varying(10), _1042 character(10), _23 integer, _21 smallint );" + 
			"INSERT INTO datatypes (_20, _1043, _1042, _23, _21) VALUES (8200000000, 'test', 'test', 200000000, 32000);", new Handler<Transaction>() {
					@Override
					public void handle(Transaction trx) {
						trx.query("SELECT * FROM datatypes", new ResultListener() {
							@Override
							public void start(Columns cols, Transaction trx) {
							}

							@Override
							public void row(Row row, Transaction trx) {
								for (int i = 0; i < row.columns().count(); i++) {
									boolean b = row.asBoolean(i);
									double d = row.asDouble(i);
									float f = row.asFloat(i);
									int in = -1;
									try { in = row.asInt(i); } catch (NumberFormatException nfe) {}
									long lo = -1;
									try {lo = row.asLong(i); } catch (NumberFormatException nfe) {}
									Object o = row.asObject(i);
									short sh = -1;
									try { sh = row.asShort(i); } catch (NumberFormatException nfe) {}
									String s = row.asString(i);								
									System.out.println("boolean: " + b + " double:" + d + " float:" + f + " int:" + in + " long:" + lo + " object:" + o + 
											" class:" + o.getClass().getName() + " short:" + sh + " string:" + s);
								}
								assertEquals(row.asLong("_20"),8200000000L);
								assertEquals(row.asString("_1043"), "test");
								assertEquals(row.asString("_1042").trim(), "test"); // padded string
								assertEquals(row.asInt("_23"), 200000000);
								assertEquals(row.asShort("_21"), (short)32000);
							}

							@Override
							public void end(int count, Transaction trx) {
								testComplete();
							}
							
						});
					}
				});
			}
		});
	}
	
}
