package us.monoid.psql.async.message;

import static org.junit.Assert.*;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

public class ErrorResponseTest {

	private byte EOS = 0;

	@Test
	public void test() {
		Buffer buffer = new Buffer();
		buffer.appendByte((byte)'E');
		int start = buffer.length();
		buffer.appendInt(-1);
		buffer.appendByte((byte)'S'); // Severity field type
		buffer.appendString("ERROR").appendByte(EOS);
		buffer.appendByte((byte)'M'); // human message
		buffer.appendString("Unit testing is good for you").appendByte(EOS);
		buffer.appendByte(EOS); // last field
		buffer.setInt(start, buffer.length() - start); // set size
		
		ErrorResponse err = new ErrorResponse();
		err.setBuffer(buffer);
		System.out.println(err.toString());
		assertTrue(err.toString().contains("good"));
	}

}
