package us.monoid.psql.async.converter;

import org.vertx.java.core.buffer.Buffer;

/** Converts the raw bytes of a value in a row to various Java formats.
 * Converters holds instances of available converters.
 * Converters can't hold onto state as they are re-used for any value */
public abstract class Converter {
	public abstract String toString(Buffer buffer, int pos, int len);
	public abstract boolean toBoolean(Buffer buffer, int pos, int len);
	public abstract int toInt(Buffer buffer, int pos, int len);
	public abstract long toLong(Buffer buffer, int pos, int len);
	public abstract double toDouble(Buffer buffer, int pos, int len);
	public abstract float toFloat(Buffer buffer, int pos, int len);
	public abstract short toShort(Buffer buffer, int pos, int len);
	public abstract char toChar(Buffer buffer, int pos, int len);
	public abstract Object toObject(Buffer buffer, int pos, int len);
}
