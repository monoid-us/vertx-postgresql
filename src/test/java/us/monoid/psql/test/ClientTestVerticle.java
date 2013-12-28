package us.monoid.psql.test;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import us.monoid.psql.async.Postgres;
import us.monoid.psql.async.SingleResult;
import us.monoid.psql.async.Transaction;

/** This is a test verticle which receives String messages and treats them as queries which return a single result (which can be converted to a string)
 * 
 * @author beders
 *
 */
public class ClientTestVerticle extends Verticle {
	Postgres pg;
	
	public void start() {
		pg = new Postgres(vertx, "test", "test".toCharArray(), "test"); 
		System.out.println("Registering handler for quer.me");
		vertx.eventBus().registerHandler("query.me", new Handler<Message<String>>() {
			@Override
			public void handle(final Message<String> msg) {
				System.out.println("Received message:" + msg);
				pg.withTransaction(new Handler<Transaction>() {
					@Override
					public void handle(Transaction trx) {
						executeSingleResultQuery(trx, msg);
					}
				});
			}
		});
	}
	
	private void executeSingleResultQuery(Transaction trx, final Message<String> msg) {
		System.out.println("Running query:" + msg.body());
		trx.query(msg.body(), new SingleResult<String>() {
			@Override
			public void result(String result, Transaction trx) {
				trx.release();
				msg.reply(result);
			}
		});
	}			
}
