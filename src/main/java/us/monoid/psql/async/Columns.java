package us.monoid.psql.async;

/** Represents a set of columns from a query Result.
 * Use this to access column names and types. 
 * 
 * @author beders
 */

public class Columns {
	Column[] columns;
	

	 public Columns(short columnCount) {
		columns = new Column[columnCount];
	}

	public static class Column {
		public final String name;
		public final Type type;
		int oid; // can be removed again once we have all types supported
		
		Column(String aName, Type aType, int anOid) {
			name = aName;
			type = aType;
			oid = anOid;
		}
	}

	public void setColumn(int i, String name, int type) {
		columns[i] = new Column(name, Types.lookup(type), type);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Column c : columns) {
			sb.append(c.name).append(" | ").append(c.type.name).append('\n');
		}
		return sb.toString();
	}
	
//	// thanks to Mauricio Linhares for putting this list together.
	// replaced by a more flexible type system
//	enum Type {
//		Bigserial(20), BigserialArray(1016), Char(18), CharArray(1002), Smallint(21), SmallintArray(1005), Integer(23), IntegerArray(1007), Numeric(
//				1700),
//		NumericArray(1231), Real(700), RealArray(1021), Double(701), DoubleArray(1022), Serial(23), Bpchar(1042), BpcharArray(1014), Varchar(
//				1043),
//		VarcharArray(1015), Text(25), TextArray(1009), Timestamp(1114), TimestampArray(1115), TimestampWithTimezone(1184), TimestampWithTimezoneArray(
//				1185), Date(1082), DateArray(1182), Time(1083), TimeArray(1183), TimeWithTimezone(1266), TimeWithTimezoneArray(1270), Interval(1186), IntervalArray(
//				1187), Boolean(16), BooleanArray(1000), OID(26), OIDArray(1028),
//
//		ByteA(17), ByteA_Array(1001),
//
//		MoneyArray(791), NameArray(1003), UUIDArray(2951), XMLArray(143), Unknown(0);
//
//		int typeID; // use catalog pg_type to 
//
//		Type(int aTypeID) {
//			typeID = aTypeID;
//		}
//
//		public static Type getType(int type) {
//			for (Type t : values()) { // yes, we could use a Map for that, but this isn't performance critical as it will be called only once for each type per query result
//				if (t.typeID == type) {
//					return t;
//				}
//			}
//			return Unknown;
//		}
//
//	}

	
}
