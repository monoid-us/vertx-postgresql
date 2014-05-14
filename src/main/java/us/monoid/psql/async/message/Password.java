package us.monoid.psql.async.message;

import org.vertx.java.core.buffer.Buffer;

public class Password extends FrontendMessage {

	public Password(Buffer buffer) {
		super(buffer);
	}
	
	public Buffer write(char[] password) {
		int rec = startRecord('p');
		cString(password);
		stopRecord(rec);
		return buffer;
	}

	public Buffer write(byte[] encodedPassword) {
		int rec = startRecord('p');
		buffer.appendBytes(encodedPassword);
		zero();
		stopRecord(rec);
		return buffer;
	}

}
