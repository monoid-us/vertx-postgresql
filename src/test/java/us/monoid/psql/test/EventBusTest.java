package us.monoid.psql.test;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/** Register a verticle with the event bus, send events and check if the single-threaded execution of several transactions works */
public class EventBusTest extends TestVerticle {
	int count;
	int expected;
	@Test
	public void testPostgres() {
		count = 0;
		expected = 10;
		container.deployVerticle("us.monoid.psql.test.ClientTestVerticle",new Handler<AsyncResult<String>>() {			
			@Override
			public void handle(AsyncResult<String> event) {
				System.out.println("registered handler: " + event.result());
				for (int i = 0; i < 10; i++ ) sendEvent();
			}
		});
	}

	protected void sendEvent() {
		vertx.eventBus().send("query.me", "select '' || count(*) from pg_tables", new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> event) {
				count++;
				if (count == expected) {
					testComplete();
				}
				System.out.println("Count: " + count);
			}
		});
	}

	@Test
	public void testMassEvents() {
		count = 0;
		expected = 10000;
		container.deployVerticle("us.monoid.psql.test.ClientTestVerticle",new Handler<AsyncResult<String>>() {			
			@Override
			public void handle(AsyncResult<String> event) {
				System.out.println("registered handler: " + event.result());
				for (int i = 0; i < 10000; i++ ) sendEvent();
			}
		});
	}
	
}
