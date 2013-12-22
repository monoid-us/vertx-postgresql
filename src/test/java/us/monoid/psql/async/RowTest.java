package us.monoid.psql.async;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import us.monoid.psql.async.message.DataRow;

public class RowTest {

	@Test
	public void simpleTest() throws IOException {
		Row r = createRow();
		String asString = r.asString(0);
		assertEquals(asString, "bubu");
		int i = r.asInt(1);
		assertEquals(42,i);
		assertTrue(r.isNull(2));
		assertEquals(0L, r.asLong(2));
		
		Buffer t = new Buffer();
		r.appendBytes(t, 0);
		System.out.println(t.length());
		assertEquals(t.getString(0, t.length()), "bubu");
	}

	private Row createRow() throws IOException {
		Buffer b = createDataRowBuffer();
		DataRow dr = new DataRow();
		dr.setBuffer(b);
		
		Columns c = new Columns((short)3);
		c.setColumn(0, "string", 1043); // varchar
		c.setColumn(1, "int", 23); // integer
		c.setColumn(2, "long", 20); // long
		Row r = new Row(c);
		r.setRow(dr);
		return r;
	}

	/** Example result with 3 columns: string, int, null 
	 * @throws IOException */
	private Buffer createDataRowBuffer() throws IOException {
		Buffer b = new Buffer();
		b.appendByte((byte)'D');
		int start = b.length();
		b.appendInt(0);
		b.appendShort((short)3);
		byte[] s = "bubu".getBytes("UTF-8");
		b.appendInt(s.length);
		b.appendBytes(s);
		b.appendInt(4);
		b.appendInt(42);
		b.appendInt(-1);
		return b;
	}

}
