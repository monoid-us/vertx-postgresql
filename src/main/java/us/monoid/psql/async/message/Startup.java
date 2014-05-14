package us.monoid.psql.async.message;

import org.vertx.java.core.buffer.Buffer;

public class Startup extends FrontendMessage {
	
	public Startup(Buffer aBuffer) { super(aBuffer); };
	
	public Buffer write(String user, String database, String applicationName) {	
		int start = startRecord();
		int16(3);
		int16(0); // protocol version
		cString("user");
		cString(user);
		cString("database");
		cString(database);
		cString("application_name");
		cString(applicationName);
		zero();
		stopRecord(start);
		return buffer;
	}

}
