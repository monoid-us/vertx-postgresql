package us.monoid.psql.async.message;

import org.vertx.java.core.buffer.Buffer;

public class Password extends Message {

	public Password(Buffer buffer) {
		super(buffer);
	}
	
	public Buffer write(char[] password) {
		int rec = startRecord('p');
		cString(password);
		stopRecord(rec);
		return buffer;
	}

}
