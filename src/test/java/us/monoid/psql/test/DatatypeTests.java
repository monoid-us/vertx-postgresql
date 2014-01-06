package us.monoid.psql.test;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import us.monoid.psql.async.Columns;
import us.monoid.psql.async.Postgres;
import us.monoid.psql.async.Row;
import us.monoid.psql.async.Transaction;
import us.monoid.psql.async.callback.ResultListener;
import static org.vertx.testtools.VertxAssert.*;

public class DatatypeTests extends TestVerticle {

	
	/** Create a table with all supported data types and test if the converters work for the returned values */
	@Test
	public void dataTypeTest() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		p.withTransaction(new Handler<Transaction>() {			
			@Override
			public void handle(Transaction trx) {
				trx.execute(
						"CREATE TEMP TABLE datatypes ( " + 
								"_20 bigint, " +
								"_1043 character varying(10), " +
								"_1042 character(10), " +
								"_23 integer, " +
								"_21 smallint, " +
								"_114 json " +
								");" + 
						"INSERT INTO datatypes (_20, _1043, _1042, _23, _21, _114) VALUES (8200000000, 'test', 'test', 200000000, 32000, " +
							"'{\"name\": \"Adams\", \"age\": 42}'::json" +
								");", new Handler<Transaction>() {
					@Override
					public void handle(Transaction trx) {
						trx.query("SELECT * FROM datatypes", new ResultListener() {
							@Override
							public void start(Columns cols, Transaction trx) {
							}
	
							@Override
							public void row(Row row, Transaction trx) {
								System.out.println("Row:" + row);
								for (int i = 0; i < row.columns().count(); i++) {
									boolean b = row.asBoolean(i);
									double d = -1;
									try {  d = row.asDouble(i); } catch (NumberFormatException nfe) {}
									float f = -1;
									try { f = row.asFloat(i); } catch (NumberFormatException nfe) {}
									int in = -1;
									try { in = row.asInt(i); } catch (NumberFormatException nfe) {}
									long lo = -1;
									try {lo = row.asLong(i); } catch (NumberFormatException nfe) {}
									Object o = row.asObject(i);
									short sh = -1;
									try { sh = row.asShort(i); } catch (NumberFormatException nfe) {}
									String s = row.asString(i);								
									System.out.println("Col: " + i + " boolean: " + b + " double:" + d + " float:" + f + " int:" + in + " long:" + lo + " object:" + o + 
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