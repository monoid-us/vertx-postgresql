package us.monoid.psql.async;

import org.vertx.java.core.buffer.Buffer;

import us.monoid.psql.async.converter.Converter;
import us.monoid.psql.async.message.DataRow;

/** Represents a row of data. Uses the fly-weight pattern, i.e. don't hold onto instances of this class. Just use the row instance passed into
 * your ResultListener to copy the data.
 *
 * For each row, access the data for each column using one of the asXxxx methods. You can either use the zero-based column index or the column name.
 * 
 * Row does some conversions for you. If you call asString(..) on a value that is a number, it will be converted to a String. 
 * However, trying to call asInt, asLong, asShort on a value that is not parseable will result in a NumberFormatException
 * 
 * Also note the various implementations of ResultListener which makes retrieving data a bit easier in may cases
 * @see us.monoid.psql.async.callback.ResultListener
 * @see us.monoid.psql.async.callback.SingleResult
 * @see us.monoid.psql.async.callback.JsonResult
 **/
public class Row {
	Columns columns;
	DataRow row;
	
	Row(Columns someColumns) {
		columns = someColumns;
	}
	
	public Columns columns() {
		return columns;
	}

	 void setRow(DataRow dataRow) {
		row = dataRow;
	}
	 
	 // ---------------- column by index
	 
	public String asString(int col) {
		// look up a suitable converter for this column
		return converter(col).toString(row.getBuffer(), row.pos(col), row.len(col), isBinary(col));
	}

	public boolean asBoolean(int col) {
		return converter(col).toBoolean(row.getBuffer(), row.pos(col), row.len(col), isBinary(col));
	}
	
	public int asInt(int col) {
		return converter(col).toInt(row.getBuffer(), row.pos(col), row.len(col), isBinary(col));
	}

	public short asShort(int col) {
		return converter(col).toShort(row.getBuffer(), row.pos(col), row.len(col), isBinary(col));
	}
	
	public long asLong(int col) {
		return converter(col).toLong(row.getBuffer(), row.pos(col), row.len(col), isBinary(col));
	}
	
	public Object asObject(int col) {
		return converter(col).toObject(row.getBuffer(), row.pos(col), row.len(col), isBinary(col));
	}
	
	public float asFloat(int col) {
		return converter(col).toFloat(row.getBuffer(), row.pos(col), row.len(col), isBinary(col));
	}
	
	public double asDouble(int col) {
		return converter(col).toDouble(row.getBuffer(), row.pos(col), row.len(col), isBinary(col));
	}
	
	/** Get a copy of the bytes for the requested column */
	public Buffer asBuffer(int col) {
		return row.getRawBytes(col);
	}
	
	// ---------------- column by name
	
	public String asString(String colName) {
		int col = columns().index(colName);
		return asString(col);
	}

	public boolean asBoolean(String colName) {
		int col = columns().index(colName);
		return asBoolean(col);
	}
	
	public int asInt(String colName) {
		int col = columns().index(colName);
		return asInt(col);
	}

	public short asShort(String colName) {
		int col = columns().index(colName);
		return asShort(col);
	}
	
	public long asLong(String colName) {
		int col = columns().index(colName);
		return asLong(col);
	}
	
	public Object asObject(String colName) {
		int col = columns().index(colName);
		return asObject(col);
	}
	
	public float asFloat(String colName) {
		int col = columns().index(colName);
		return asFloat(col);
	}
	
	public double asDouble(String colName) {
		int col = columns().index(colName);
		return asDouble(col);
	}
	
	/** Get a copy of the bytes for the requested column */
	public Buffer asBuffer(String colName) {
		int col = columns().index(colName);
		return asBuffer(col);
	}

	
	/** Return the length of a column in bytes. Might return -1 in which case the value is NULL */
	public int length(int col) {
		return row.len(col);
	}
	
	/** Append bytes for column 'col' to target buffer. Return the number of bytes written.
	 * @return number of bytes copied
	 * This should work for Strings / varchar / character varying if both buffers use UTF8 as String encoding
	 */
	public int appendBytes(Buffer target, int col) {
		target.appendBuffer(row.getRawBytes(col));
		/* ByteBuf targetBB = target.getByteBuf();
		ByteBuf srcBB = row.getBuffer().getByteBuf();
		int len = row.len(col);
		targetBB.writeBytes(srcBB, row.pos(col), len); // this is a copy of the actual target buffer :(*
		*/
		return row.len(col);
	}
	
	private Converter converter(int col) {
//		System.out.println("OID: " + columns.columns[col].oid);
//		System.out.println("Name: " + columns.columns[col].name);
//		System.out.println("Converter:" + columns.columns[col].type.converter.getClass());
		return columns.columns[col].type.converter;
	}
	
	/** Return true if data encoded in the row is text vs binary format */
	private boolean isBinary(int col) {
		return columns.columns[col].isBinary();
	}

	/** Check if a column has the NULL value */
	public boolean isNull(int col) {
		return length(col) < 0;
	}

}
