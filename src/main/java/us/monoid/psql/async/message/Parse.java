package us.monoid.psql.async.message;

import org.vertx.java.core.buffer.Buffer;

/** Parse message to prepare a query using the extended protocol.
 * Relies on caller giving appropriate type information. 
 * 
 * @author beders
 *
 */
public class Parse extends FrontendMessage {

	public Parse(Buffer buffer) {
		super(buffer);
	}
	
	public Buffer write(String sqlString, String prepStatement, int[] paramTypes) {
		int start = startRecord('P');
		cString(prepStatement);
		cString(sqlString);
		int16(paramTypes.length);
		for (int type : paramTypes) {
			int32(type);
		}
		stopRecord(start);
		return buffer;
	}

}
