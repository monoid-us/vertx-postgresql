package us.monoid.psql.async.converter;

import org.vertx.java.core.buffer.Buffer;

/**
 * Converter for whole numbers of length 2,4 and 8. If column value is NULL, returns 0L.
 **/
public class NumberConverter extends Converter {
	int typeSize;

	NumberConverter(int aTypeSize) {
		typeSize = aTypeSize;
	}

	@Override
	public String toString(Buffer buffer, int pos, int len, boolean isBinary) {
		if (isBinary) {
			return Long.toString(readNumber(buffer, pos, len, isBinary));
		} else {
			return Converters.stringConverter.toString(buffer, pos, len, isBinary);
		}
	}

	@Override
	public boolean toBoolean(Buffer buffer, int pos, int len, boolean isBinary) {
		return readNumber(buffer, pos, len, isBinary) == 0L;
	}

	@Override
	public int toInt(Buffer buffer, int pos, int len, boolean isBinary) {
		return (int) readNumber(buffer, pos, len, isBinary);
	}

	@Override
	public long toLong(Buffer buffer, int pos, int len, boolean isBinary) {
		return readNumber(buffer, pos, len, isBinary);
	}

	@Override
	public short toShort(Buffer buffer, int pos, int len, boolean isBinary) {
		return (short) readNumber(buffer, pos, len, isBinary);
	}

	@Override
	public double toDouble(Buffer buffer, int pos, int len, boolean isBinary) {
		return (double) readNumber(buffer, pos, len, isBinary);
	}

	@Override
	public float toFloat(Buffer buffer, int pos, int len, boolean isBinary) {
		return (float) readNumber(buffer, pos, len, isBinary);
	}

	@Override
	public char toChar(Buffer buffer, int pos, int len, boolean isBinary) {
		return (char) readNumber(buffer, pos, len, isBinary);
	}

	@Override
	public Object toObject(Buffer buffer, int pos, int len, boolean isBinary) {
		long number = readNumber(buffer, pos, len, isBinary);
		switch (typeSize) { // indendent of len, we need to return the correct boxed type
		case 2:
			return (short) number;
		case 4:
			return (int) number;
		case 8:
			return (long) number;
		}
		return 0;
	}

	long readNumber(Buffer buffer, int pos, int len, boolean isBinary) {
		if (len == -1) return 0L;
		if (isBinary) {
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
		} else { // textual representation of the number
			long parseLong = Long.parseLong(Converters.stringConverter.toString(buffer, pos, len, isBinary));
			return parseLong;
		}
	}
}
