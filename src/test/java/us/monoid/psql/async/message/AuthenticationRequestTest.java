package us.monoid.psql.async.message;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

public class AuthenticationRequestTest {

	@Test
	public void test() {
		Buffer msg = new Buffer();
		msg.appendByte((byte)'R');
		msg.appendInt(8);
		msg.appendInt(3);
		AuthenticationRequest ar = new AuthenticationRequest();
		ar.setBuffer(msg);
		assertTrue(ar.isAuthenticationSupported());
		assertTrue(ar.needCleartextPassword());
		msg.setInt(5, 0);
		
		ar = new AuthenticationRequest();
		ar.setBuffer(msg);
		assertTrue(ar.isAuthenticationOk());
		
		msg.setInt(1, 12);
		msg.setInt(5, 5);
		byte[] salt = { 1,2,3,4 };
		msg.appendBytes(salt);
		
		ar = new AuthenticationRequest();
		ar.setBuffer(msg);
		assertTrue(ar.isAuthenticationSupported());
		assertTrue(ar.needMD5Password());
		assertTrue(Arrays.equals(salt, ar.salt()));
	}

}
