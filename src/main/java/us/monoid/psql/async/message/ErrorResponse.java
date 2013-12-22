package us.monoid.psql.async.message;


/** An error was received from the back-end side */
public class ErrorResponse extends BackendMessage {


	public String toString() {
		StringBuilder sb = new StringBuilder(messageLength());
		int pos = 5;
		
		byte fieldType = buffer.getByte(pos++);
		while (fieldType != 0 && pos < messageLength()) {
			sb.append((char) fieldType).append(':');
			pos = readCString(pos, sb);
			sb.append(' ');
			fieldType = buffer.getByte(pos++);
		}
		return sb.toString();
	}

	
}
