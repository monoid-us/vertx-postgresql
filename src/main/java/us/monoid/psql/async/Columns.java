package us.monoid.psql.async;

/**
 * Represents a set of columns from a query Result. Use this to access column names and types.
 * 
 * @author beders
 */

public class Columns {
	final Column[] columns;

	public Columns(short columnCount) {
		columns = new Column[columnCount];
	}

	/** Get a column by index. Note that this is a zero-based index, i.e. first column is getColumn(0) */
	public Column get(int col) {
		return columns[col];
	}
	
	/** Get a column by name. */
	public Column get(String name) {
		for (Column c : columns) {
			if (c.name.equals(name)) {
				return c;
			}
		}
		return null;
	}
	
	/** Get the index of the column by name. Returns -1 if column is not found.
	 * You can use this method with the various Row methods to retrieve the value of a column
	 * {@code row.asString(row.columns().index("name")); } 
	 **/
	public int index(String name) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].name.equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public static class Column {
		public final String name;
		public final Type type;
		int oid; // can be removed again once we have all types supported
		private final short formatCode;

		Column(String aName, Type aType, int anOid, short aFormatCode) {
			name = aName;
			type = aType;
			oid = anOid;
			formatCode = aFormatCode;
		}

		public boolean isBinary() {
			return formatCode == 1;
		}

	}

	/** Add column information. This method is used internally */
	public void setColumn(int i, String name, int type, short formatCode) {
		columns[i] = new Column(name, Types.lookup(type), type, formatCode);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Column c : columns) {
			sb.append(c.name).append(" | ").append(c.type.name).append(" | ").append(c.formatCode == 0 ? "text" : "binary").append('\n');
		}
		return sb.toString();
	}

	/** Return number of columns */
	public int count() {
		return columns.length;
	}

	// // thanks to Mauricio Linhares for putting this list together.
	// replaced by a more flexible type system
	// enum Type {
	// Bigserial(20), BigserialArray(1016), Char(18), CharArray(1002), Smallint(21), SmallintArray(1005), Integer(23), IntegerArray(1007), Numeric(
	// 1700),
	// NumericArray(1231), Real(700), RealArray(1021), Double(701), DoubleArray(1022), Serial(23), Bpchar(1042), BpcharArray(1014), Varchar(
	// 1043),
	// VarcharArray(1015), Text(25), TextArray(1009), Timestamp(1114), TimestampArray(1115), TimestampWithTimezone(1184), TimestampWithTimezoneArray(
	// 1185), Date(1082), DateArray(1182), Time(1083), TimeArray(1183), TimeWithTimezone(1266), TimeWithTimezoneArray(1270), Interval(1186), IntervalArray(
	// 1187), Boolean(16), BooleanArray(1000), OID(26), OIDArray(1028),
	//
	// ByteA(17), ByteA_Array(1001),
	//
	// MoneyArray(791), NameArray(1003), UUIDArray(2951), XMLArray(143), Unknown(0);
	//
	// int typeID; // use catalog pg_type to
	//
	// Type(int aTypeID) {
	// typeID = aTypeID;
	// }
	//
	// public static Type getType(int type) {
	// for (Type t : values()) { // yes, we could use a Map for that, but this isn't performance critical as it will be called only once for each type per query result
	// if (t.typeID == type) {
	// return t;
	// }
	// }
	// return Unknown;
	// }
	//
	// }

}
