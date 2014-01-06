package us.monoid.psql.test;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/** Register a verticle with the event bus, send events and check if the single-threaded execution of several transactions works */
public class LoadTest extends TestVerticle {
	private static final int RUNS = 1000; // try 100.000 for kicks
	int count;
	int expected;
	long start;
	
	protected void sendEvent(String query) {
		vertx.eventBus().send("query.me", query, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> event) {
				count++;
				if (count == expected) {
					System.out.println("Raw time:" + (System.currentTimeMillis() - start));
					testComplete();
				}
				System.out.println("Count: " + count);
			}
		});
	}

	@Test
	public void testMassEventsOneVerticle() {
		count = 0;
		expected = RUNS;
		start = System.currentTimeMillis();
		container.deployVerticle("us.monoid.psql.test.ClientTestVerticle",new Handler<AsyncResult<String>>() {			
			@Override
			public void handle(AsyncResult<String> event) {
				System.out.println("registered handler: " + event.result());
				for (int i = 0; i < RUNS; i++ ) sendEvent("select '' || count(*) from pg_tables");
			}
		});
	}
	
	@Test
	public void testMassEventsFourVerticles() {
		count = 0;
		expected = RUNS;
		start = System.currentTimeMillis();
		container.deployVerticle("us.monoid.psql.test.ClientTestVerticle", 4, new Handler<AsyncResult<String>>() {			
			@Override
			public void handle(AsyncResult<String> event) {
				System.out.println("registered handler: " + event.result());
				for (int i = 0; i < RUNS; i++ ) sendEvent("select '' || count(*) from pg_tables");
			}
		});
	}
	
	
	@Test
	public void testCommitOneVerticle() {
		count = 0;
		expected = RUNS;
		start = System.currentTimeMillis();
		container.deployVerticle("us.monoid.psql.test.ClientTestVerticle",new Handler<AsyncResult<String>>() {			
			@Override
			public void handle(AsyncResult<String> event) {
				System.out.println("registered handler: " + event.result());
				for (int i = 0; i < RUNS; i++ ) sendEvent("begin; select '' || count(*) from pg_tables; commit");
			}
		});
	}
}
