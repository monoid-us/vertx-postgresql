package us.monoid.psql.async.converter;

import org.vertx.java.core.buffer.Buffer;

/** Converter for whole numbers of length 2,4 and 8.
 * If column value is NULL, returns 0L.
 **/
public class NumberConverter extends Converter {
	int typeSize;
	
	NumberConverter(int aTypeSize) {
		typeSize = aTypeSize;
	}
	
	@Override
	public String toString(Buffer buffer, int pos, int len) {
		return Long.toString(readNumber(buffer, pos, len));
	}

	@Override
	public boolean toBoolean(Buffer buffer, int pos, int len) {
		return readNumber(buffer, pos, len) == 0L;
	}

	long readNumber(Buffer buffer, int pos, int len) {
		switch (len) {
		case -1:
		case -2:
			return 0L;
		case 2:
			return buffer.getShort(pos);
		case 4:
			return buffer.getInt(pos);
		case 8:
			return buffer.getLong(pos);
			default:
				return 0L;
		}
	}
	
	@Override
	public int toInt(Buffer buffer, int pos, int len) {
		return (int) readNumber(buffer, pos, len);
	}

	@Override
	public long toLong(Buffer buffer, int pos, int len) {
		return readNumber(buffer, pos, len);
	}
	
	@Override
	public short toShort(Buffer buffer, int pos, int len) {
		return (short)readNumber(buffer, pos, len);
	}

	@Override
	public double toDouble(Buffer buffer, int pos, int len) {
		return (double)readNumber(buffer, pos, len);
	}

	@Override
	public float toFloat(Buffer buffer, int pos, int len) {
		return (float)readNumber(buffer, pos, len);
	}

	@Override
	public char toChar(Buffer buffer, int pos, int len) {
		return (char)readNumber(buffer, pos, len);
	}

	@Override
	public Object toObject(Buffer buffer, int pos, int len) {
		long number = readNumber(buffer, pos, len);
		switch (typeSize) { // indendent of len, we need to return the correct boxed type
		case 2:
			return (short)number;
		case 4:
			return (int)number;
		case 8:
			return (long)number;
		}
		return 0;
	}

}
