package us.monoid.psql.async.message;

import org.vertx.java.core.buffer.Buffer;

public class Query extends FrontendMessage {

	public Query(Buffer buffer) {
		super(buffer);
	}

	public Buffer write(String sqlString) {
		int start = startRecord('Q');
		cString(sqlString);
		stopRecord(start);
		return buffer;
	}

}
