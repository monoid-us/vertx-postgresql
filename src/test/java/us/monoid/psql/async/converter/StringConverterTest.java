package us.monoid.psql.async.converter;

import static org.junit.Assert.*;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

public class StringConverterTest {

	@Test
	public void test() {
		StringConverter c = new StringConverter();
		
		Buffer b = createBufferForString("bubu");
		int pos = 0;
		int len = b.length();
		
		String s = c.toString(b, pos, len, false);
		assertEquals(s, "bubu");
		
		boolean bo = c.toBoolean(b, pos, len, false);
		assertFalse(bo); // string isn't 'true'
		
		char char1 = c.toChar(b, pos, len, false);
		assertEquals(char1, "bubu".charAt(0));
		
		try {
			c.toDouble(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
		
		try {
			c.toFloat(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
		
		try {
			c.toInt(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
		
		try {
			c.toLong(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
		

		try {
			c.toShort(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
	}
	
	@Test
	public void testNull() {
		Buffer b = new Buffer();
		StringConverter c = new StringConverter();
		int pos = 0, len = -1;
		String string = c.toString(b, pos, len, false);
		assertEquals(string, "NULL");
		
		boolean bo = c.toBoolean(b, pos, len, false);
		assertFalse(bo); // string isn't 'true'
		
		char char1 = c.toChar(b, pos, len, false);
		assertEquals(char1, "N".charAt(0));
		
		try {
			c.toDouble(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
		
		try {
			c.toFloat(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
		
		try {
			c.toInt(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
		
		try {
			c.toLong(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
		

		try {
			c.toShort(b, pos, len, false);
			fail();
		} catch (NumberFormatException nfe) {
		}
	}
	
	
	public Buffer createBufferForString(String aString) {
		Buffer buffer = new Buffer(aString.length());
		buffer.appendString(aString, "UTF8");
		return buffer;
	}

}
