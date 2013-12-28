package us.monoid.psql.async.message;

import static org.junit.Assert.*;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

public class StartupTest {

	@Test
	public void test() {
		Buffer buffer = new Buffer(100);
		Buffer buffer2 = new Startup(buffer).write("test", "db", "name");
		assertTrue(buffer2.length() > 0);
		assertTrue(buffer.toString("UTF8").contains("test"));
		assertTrue(buffer.toString("UTF8").contains("db"));
	}

}
