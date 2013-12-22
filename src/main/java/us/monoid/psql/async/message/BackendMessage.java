package us.monoid.psql.async.message;

import java.io.UnsupportedEncodingException;

import org.vertx.java.core.buffer.Buffer;

/** This base class and subclasses need to use the flyweight pattern for parsing. Consequently instances are not re-entrant.
 * See Postgres Manual 46.5. Message Formats
 * i.e. http://www.postgresql.org/docs/9.3/static/protocol-message-formats.html
 * @author beders
 *
 */

public abstract class BackendMessage {
	Buffer buffer;
	
	public BackendMessage() {
		// buffer will be set for each parsing action done by the handlers for each message
	}

	public void setBuffer(Buffer aBuffer) {
		buffer = aBuffer;
	}
	
	protected int messageLength() {
		return buffer.getInt(1);
	}

	protected int readCString(int pos, StringBuilder sb) {
		try {
			for (int end = pos, len = buffer.length(); end < len; end++) {
				if (buffer.getByte(end) == 0) {
					sb.append(new String(buffer.getBytes(pos, end), Message.UTF8));
					end++;
					return end;
				}
			}
		} catch (UnsupportedEncodingException uee) {
		} // UTF8 is just dandy
		throw new IllegalArgumentException("No c-string found at pos:" + pos + " len:" + messageLength());
	}

	protected String readCString(int pos) {
		StringBuilder sb = new StringBuilder();
		readCString(pos, sb);
		return sb.toString();
	}
}
