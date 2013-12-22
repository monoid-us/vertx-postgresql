package us.monoid.psql.test;/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertFalse;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import us.monoid.psql.async.Columns;
import us.monoid.psql.async.Postgres;
import us.monoid.psql.async.Row;
import us.monoid.psql.async.Transaction;
import us.monoid.psql.async.callback.PromisedResult;
import us.monoid.psql.async.callback.ResultListener;
import us.monoid.psql.async.callback.TrxCallback;
import us.monoid.psql.async.callback.TrxCallbackWith;
import us.monoid.psql.async.callback.TrxPromise;
import us.monoid.psql.async.promise.Promise;

/**
 * Simple integration test which shows tests deploying other verticles, using the Vert.x API etc.
 * 
 * NOTE: To run this test, you need a local PostgresDB named test with user test and password test.
 * Also make sure 'password' connections are enabled for this user. (Check if your pg_hba.conf has a line like this:
 * <pre>host    all             all             127.0.0.1/32            password</pre>
 * 
 */
public class BasicIntegrationTest extends TestVerticle {

	@Test
	public void testPostgres() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		p.withTransaction(new Handler<Transaction>() {
			@Override
			public void handle(Transaction trx) {
				assertNotNull(trx);
				assertTrue(trx.isReady());
				testComplete();
			}
		});
	}

	@Test
	public void testDDL() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		p.withTransaction(new Handler<Transaction>() {
			@Override
			public void handle(Transaction trx) {
				assertNotNull(trx);
				assertTrue(trx.isReady());
				trx.execute("CREATE TABLE test ( name varchar(15) )", new Handler<Transaction>() {
					@Override
					public void handle(Transaction trx) {
						System.out.println(trx.lastResult());
						dropTable(trx);
					}
				});
			}
		});

	}
	
	private void dropTable(Transaction trx) {
		trx.execute("DROP TABLE test").done(new TrxCallback() {
			@Override
			public void handle(Transaction trx) {
				testComplete();
			}
		});
	}

	@Test
	public void testDDL2() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		p.withTransaction(new Handler<Transaction>() {
			@Override
			public void handle(Transaction trx) {
				assertNotNull(trx);
				assertTrue(trx.isReady());
				createTable("OK", trx).then(new TrxCallbackWith<Void>() {
					@Override
					public Void handle(Transaction trx) {
						dropTable(trx);
						return null;
					}
				});
			}
		});
	}
	
	private PromisedResult createTable(String result, Transaction trx) {
		final PromisedResult p = new PromisedResult();
		trx.execute("CREATE TABLE test ( name varchar(15))").then(new TrxCallbackWith<Void>() {
			@Override
			public Void handle(Transaction transaction) {
				assertFalse(transaction.lastResultIs(null));
				assertTrue(transaction != null);
				p.fulfill(transaction);
				return null;
			}
		});
		return p;
	}

	
	@Test 
	public void simpleQuery() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		p.withTransaction(new Handler<Transaction>() {
			@Override public void handle(Transaction trx) {
				trx.execute("CREATE TABLE test ( name varchar(15)); INSERT INTO test VALUES ('bubu');").then(new TrxPromise() {
					@Override	public Promise<Transaction> handle(Transaction trx) {
						return queryTest(trx);
					}
				}).done(new TrxCallback() {					
					@Override
					public void handle(Transaction handle) {
						queryResult(handle);
					}
				});
			}
		});
	}
	
	public PromisedResult queryTest(Transaction trx) {
		return trx.execute("SELECT * FROM test");
	}
	
	public void queryResult(Transaction trx) {
		System.out.println(trx.lastResult());
		dropTable(trx);
		testComplete();
	}
	
	@Test 
	public void simpleQueryWithResult() {
		Postgres p = new Postgres(vertx, "test", "test".toCharArray(), "test");
		p.withTransaction(new Handler<Transaction>() {
			final long mem = Runtime.getRuntime().freeMemory();
			@Override public void handle(Transaction trx) {
				trx.execute("CREATE TABLE test ( name varchar(15)); INSERT INTO test VALUES ('bubu');").done(new TrxCallback() {
					@Override	public void handle(Transaction trx) {
						trx.query("SELECT * FROM test", new ResultListener() {							
							@Override
							public void start(Columns cols, Transaction trx) {
								System.out.println("Receiving Results");
								System.out.println("Memory before results:" + (mem - Runtime.getRuntime().freeMemory()));
							}
							
							@Override
							public void row(Row row, Transaction trx) {
								String result = row.asString(0);
								System.out.println(result);
								assertEquals(result,"bubu");
							}
							
							@Override
							public void end(int count, Transaction trx) {
								assertEquals(count, 1);
								dropTable(trx);
								System.out.println("Memory:" + (mem - Runtime.getRuntime().freeMemory()));
							}
						});
					}
				});
			}
		});
	}
	
	
	// @Test
	// /*
	// This test deploys some arbitrary verticle - note that the call to testComplete() is inside the Verticle `SomeVerticle`
	// */
	// public void testDeployArbitraryVerticle() {
	// assertEquals("bar", "bar");
	// container.deployVerticle(SomeVerticle.class.getName());
	// }
	//
	// @Test
	// public void testCompleteOnTimer() {
	// vertx.setTimer(1000, new Handler<Long>() {
	// @Override
	// public void handle(Long timerID) {
	// assertNotNull(timerID);
	//
	// // This demonstrates how tests are asynchronous - the timer does not fire until 1 second later -
	// // which is almost certainly after the test method has completed.
	// testComplete();
	// }
	// });
	// }
	//

}
