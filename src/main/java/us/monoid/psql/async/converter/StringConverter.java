package us.monoid.psql.async.converter;

import org.vertx.java.core.buffer.Buffer;

/** Turns character, character varying, text into Strings */
public class StringConverter extends Converter {

	@Override
	public String toString(Buffer buffer, int pos, int len) {
		if (len == -1) return "NULL";
		return buffer.getString(pos, pos + len, "UTF8");
	}

	@Override
	public boolean toBoolean(Buffer buffer, int pos, int len) {
		return Boolean.parseBoolean(toString(buffer,pos,len));
	}

	@Override
	public int toInt(Buffer buffer, int pos, int len) {
		return Integer.parseInt(toString(buffer,pos,len));
	}

	@Override
	public long toLong(Buffer buffer, int pos, int len) {
		return Long.parseLong(toString(buffer,pos,len));
	}

	@Override
	public double toDouble(Buffer buffer, int pos, int len) {
		return Double.parseDouble(toString(buffer,pos,len));
	}

	@Override
	public float toFloat(Buffer buffer, int pos, int len) {
		return Float.parseFloat(toString(buffer,pos,len));
	}

	@Override
	public short toShort(Buffer buffer, int pos, int len) {
		return Short.parseShort(toString(buffer,pos,len));
	}

	@Override
	public char toChar(Buffer buffer, int pos, int len) {
		return toString(buffer,pos,len).charAt(0);
	}

	@Override
	public Object toObject(Buffer buffer, int pos, int len) {
		return toString(buffer, pos, len);
	}
	
}
