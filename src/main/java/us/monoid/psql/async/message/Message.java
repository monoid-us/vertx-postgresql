package us.monoid.psql.async.message;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.vertx.java.core.buffer.Buffer;

/** Base class for front or back-end message that are part of the Postgresql protocol.
 * @author beders
 */
public abstract class Message {
	protected static final String UTF8 = "UTF8";
	protected static final byte EOS = 0;
	Buffer buffer;
	
	public Message(Buffer buffer) {
		this.buffer = buffer;
	}

	protected void int32(int index, int i) {
		buffer.setInt(index, i);
	}

	protected void cString(String string) {
		buffer.appendString(string, UTF8);
		buffer.appendByte(EOS);
	}
	
	protected void cString(char[] password) {
		ByteBuffer encoded = Charset.forName(UTF8).encode(CharBuffer.wrap(password));
		buffer.appendBytes(encoded.array());
		buffer.appendByte(EOS);
	}
	
	protected void int32(int value) {
		buffer.appendInt(value);
	}
	
	protected void int16(int i) {
		int16((short)i);
	}
	
	protected void int16(short value) {
		buffer.appendShort(value);
	}

	protected void zero() {
		buffer.appendByte(EOS);
	}
	/** Indicate the type of message */
	protected void bite(char c) {
		buffer.appendByte((byte)c);
	}
	
	/** Postgres demands a record structure that starts with the length of the record. 
	 * This will return the index where the length should be stored and writes -1 as current length 
	*/
	protected int startRecord() {
		int start = buffer.length();
		int32(-1);
		return start;
	}
	
	/** Postgres demands a record structure that starts with the length of the record. 
	 * This will set the message type, return the index where the length should be stored and writes -1 as current length 
	 * @param c */
	protected int startRecord(char c) {
		bite(c);
		return startRecord();
	}
	
	/** Postgres demands a record structure that starts with the length of the record
	 * Call this with the value from startRecord to finishe the record and update the length field  */
	protected void stopRecord(int start) {
		int32(start, buffer.length() - start);
	}
	
}
