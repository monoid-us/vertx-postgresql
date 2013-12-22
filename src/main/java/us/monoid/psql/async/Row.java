package us.monoid.psql.async;

import us.monoid.psql.async.converter.Converter;
import us.monoid.psql.async.message.DataRow;

/** Represents a row of data. Uses the flyweight pattern, i.e. don't hold onto instances of this class */
public class Row {
	Columns columns;
	DataRow row;
	
	public Row(Columns someColumns) {
		columns = someColumns;
	}
	
	public Columns columns() {
		return columns;
	}

	 void setRow(DataRow dataRow) {
		row = dataRow;		
	}
	 
	public String asString(int col) {
		// look up a suitable converter for this column
		return converter(col).toString(row.getBuffer(), row.pos(col), row.len(col));
	}

	public int asInt(int col) {
		return converter(col).toInt(row.getBuffer(), row.pos(col), row.len(col));
	}

	public Object asObject(int col) {
		return converter(col).toObject(row.getBuffer(), row.pos(col), row.len(col));
	}

	private Converter converter(int col) {
		System.out.println("OID: " + columns.columns[col].oid);
		System.out.println("Name: " + columns.columns[col].name);
		System.out.println("Converter:" + columns.columns[col].type.converter.getClass());
		return columns.columns[col].type.converter;
	}

}
