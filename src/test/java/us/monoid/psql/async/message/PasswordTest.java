package us.monoid.psql.async.message;

import static org.junit.Assert.*;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

public class PasswordTest {

	@Test
	public void test() {
		Buffer buffer = new Buffer();
		Password p = new Password(buffer);
		char[] pwd = { 't','e','s','t' };
		Buffer b = p.write(pwd);
		assertNotNull(b);
		assertTrue(b.getByte(0) == 'p');
		assertTrue(b.toString().contains("test"));
		assertTrue(b.getByte(b.length() - 1) == 0);
		assertTrue(b.length() > 0);
	}

}
