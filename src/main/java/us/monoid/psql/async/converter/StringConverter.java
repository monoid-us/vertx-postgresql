package us.monoid.psql.async.converter;

import org.vertx.java.core.buffer.Buffer;

/** Turns character, character varying, text into Strings */
public class StringConverter extends Converter {
	
	@Override
	public String toString(Buffer buffer, int pos, int len, boolean isBinary) {
		if (len == -1) return "NULL";
		return buffer.getString(pos, pos + len, "UTF8");
	}

	@Override
	public boolean toBoolean(Buffer buffer, int pos, int len, boolean isBinary) {
		return Boolean.parseBoolean(toString(buffer,pos,len,isBinary));
	}

	@Override
	public int toInt(Buffer buffer, int pos, int len, boolean isBinary) {
		return Integer.parseInt(toString(buffer,pos,len,isBinary));
	}

	@Override
	public long toLong(Buffer buffer, int pos, int len, boolean isBinary) {
		return Long.parseLong(toString(buffer,pos,len,isBinary));
	}

	@Override
	public double toDouble(Buffer buffer, int pos, int len, boolean isBinary) {
		return Double.parseDouble(toString(buffer,pos,len,isBinary));
	}

	@Override
	public float toFloat(Buffer buffer, int pos, int len, boolean isBinary) {
		return Float.parseFloat(toString(buffer,pos,len,isBinary));
	}

	@Override
	public short toShort(Buffer buffer, int pos, int len, boolean isBinary) {
		return Short.parseShort(toString(buffer,pos,len,isBinary));
	}

	@Override
	public char toChar(Buffer buffer, int pos, int len, boolean isBinary) {
		return toString(buffer,pos,len,isBinary).charAt(0);
	}

	@Override
	public Object toObject(Buffer buffer, int pos, int len, boolean isBinary) {
		return toString(buffer, pos, len,isBinary);
	}
	
}
