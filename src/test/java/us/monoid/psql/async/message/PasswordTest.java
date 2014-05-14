package us.monoid.psql.async.message;

import static org.junit.Assert.*;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import us.monoid.psql.async.auth.MD5Digest;

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

	@Test
	public void testMD5() {
		Buffer buffer = new Buffer();
		Password p = new Password(buffer);
		char[] pwd = { 't','e','s','t' };
		byte[] salt = { 1, 2, 3, 4 };
		Buffer b = p.write(MD5Digest.encode("test", pwd, salt));
		assertNotNull(b);
		assertTrue(b.getByte(0) == 'p');
		assertTrue(b.getByte(b.length() - 1) == 0);
		assertTrue(b.length() > 0);
	}

}
