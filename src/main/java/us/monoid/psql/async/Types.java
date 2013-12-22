package us.monoid.psql.async;

import java.util.HashMap;
import java.util.Map;

import us.monoid.psql.async.converter.Converters;

/**
 * Built-in PostgresQL times. Some types are commented out as they are used internally mostly
 * 
 * @author beders
 * 
 */
public class Types {
	static Map<Integer,Type> types = new HashMap<>();
	
	// public static final Type "Any" = new Type(""any"",2276,4); //
	public static final Type Abstime = new Type("abstime", 702, 4); // absolute, limited-range date and time (Unix system time)
	public static final Type AbstimeArray = new Type("abstime[]", 1023, -1); //
	public static final Type Aclitem = new Type("aclitem", 1033, 12); // access control list
	public static final Type AclitemArray = new Type("aclitem[]", 1034, -1); //
	// public static final Type Anyarray = new Type("anyarray",2277,-1); //
	// public static final Type Anyelement = new Type("anyelement",2283,4); //
	// public static final Type Anyenum = new Type("anyenum",3500,4); //
	// public static final Type Anynonarray = new Type("anynonarray",2776,4); //
	// public static final Type Anyrange = new Type("anyrange",3831,-1); //
	public static final Type Bigint = new Type("bigint", 20, 8, Converters.longConverter); // ~18 digit integer, 8-byte storage
	public static final Type BigintArray = new Type("bigint[]", 1016, -1); //
	public static final Type Bit = new Type("bit", 1560, -1); // fixed-length bit string
	public static final Type BitVarying = new Type("bit varying", 1562, -1); // variable-length bit string
	public static final Type BitVaryingArray = new Type("bit varying[]", 1563, -1); //
	public static final Type BitArray = new Type("bit[]", 1561, -1); //
	public static final Type Boolean = new Type("boolean", 16, 1); // boolean, 'true'/'false'
	public static final Type BooleanArray = new Type("boolean[]", 1000, -1); //
	public static final Type Box = new Type("box", 603, 32); // geometric box '(lower left,upper right)'
	public static final Type BoxArray = new Type("box[]", 1020, -1); //
	public static final Type Bytea = new Type("bytea", 17, -1); // variable-length string, binary values escaped
	public static final Type ByteaArray = new Type("bytea[]", 1001, -1); //
	public static final Type Char = new Type("char", 18, 1); // single character
	public static final Type CharArray = new Type("char[]", 1002, -1); //
	public static final Type Character = new Type("character", 1042, -1); // char(length), blank-padded string, fixed storage length
	public static final Type CharacterVarying = new Type("character varying", 1043, -1); // varchar(length), non-blank-padded string, variable storage length
	public static final Type Varchar = new Type("varchar", 1043, -1);
	public static final Type CharacterVaryingArray = new Type("character varying[]", 1015, -1); //
	public static final Type CharacterArray = new Type("character[]", 1014, -1); //
	// public static final Type Cid = new Type("cid",29,4); // command identifier type, sequence in transaction id
	// public static final Type CidArray = new Type("cid[]",1012,-1); //
	public static final Type Cidr = new Type("cidr", 650, -1); // network IP address/netmask, network address
	public static final Type CidrArray = new Type("cidr[]", 651, -1); //
	public static final Type Circle = new Type("circle", 718, 24); // geometric circle '(center,radius)'
	public static final Type CircleArray = new Type("circle[]", 719, -1); //
	// public static final Type Cstring = new Type("cstring",2275,-2); //
	// public static final Type CstringArray = new Type("cstring[]",1263,-1); //
	public static final Type Date = new Type("date", 1082, 4); // date
	public static final Type DateArray = new Type("date[]", 1182, -1); //
	public static final Type Daterange = new Type("daterange", 3912, -1); // range of dates
	public static final Type DaterangeArray = new Type("daterange[]", 3913, -1); //
	public static final Type DoublePrecision = new Type("double precision", 701, 8); // double-precision floating point number, 8-byte storage
	public static final Type DoublePrecisionArray = new Type("double precision[]", 1022, -1); //
	// public static final Type Event_Trigger = new Type("event_trigger",3838,4); //
	// public static final Type Fdw_Handler = new Type("fdw_handler",3115,4); //
	// public static final Type Gtsvector = new Type("gtsvector",3642,-1); // GiST index internal text representation for text search
	// public static final Type GtsvectorArray = new Type("gtsvector[]",3644,-1); //
	public static final Type Inet = new Type("inet", 869, -1); // IP address/netmask, host address, netmask optional
	public static final Type InetArray = new Type("inet[]", 1041, -1); //
	// public static final Type Int2vector = new Type("int2vector",22,-1); // array of int2, used in system tables
	// public static final Type Int2vectorArray = new Type("int2vector[]",1006,-1); //
	public static final Type Int4range = new Type("int4range", 3904, -1); // range of integers
	public static final Type Int4rangeArray = new Type("int4range[]", 3905, -1); //
	public static final Type Int8range = new Type("int8range", 3926, -1); // range of bigints
	public static final Type Int8rangeArray = new Type("int8range[]", 3927, -1); //
	public static final Type Integer = new Type("integer", 23, 4, Converters.intConverter); // -2 billion to 2 billion integer, 4-byte storage
	public static final Type IntegerArray = new Type("integer[]", 1007, -1); //
	// public static final Type Internal = new Type("internal",2281,8); //
	public static final Type Interval = new Type("interval", 1186, 16); // @ <number> <units>, time interval
	public static final Type IntervalArray = new Type("interval[]", 1187, -1); //
	public static final Type Json = new Type("json", 114, -1); //
	public static final Type JsonArray = new Type("json[]", 199, -1); //
	public static final Type Language_Handler = new Type("language_handler", 2280, 4); //
	public static final Type Line = new Type("line", 628, 32); // geometric line (not implemented)
	public static final Type LineArray = new Type("line[]", 629, -1); //
	public static final Type Lseg = new Type("lseg", 601, 32); // geometric line segment '(pt1,pt2)'
	public static final Type LsegArray = new Type("lseg[]", 1018, -1); //
	public static final Type Macaddr = new Type("macaddr", 829, 6); // XX:XX:XX:XX:XX:XX, MAC address
	public static final Type MacaddrArray = new Type("macaddr[]", 1040, -1); //
	public static final Type Money = new Type("money", 790, 8); // monetary amounts, $d,ddd.cc
	public static final Type MoneyArray = new Type("money[]", 791, -1); //
	// public static final Type Name = new Type("name",19,64); // 63-byte public static final Type for storing system identifiers
	// public static final Type NameArray = new Type("name[]",1003,-1); //
	public static final Type Numeric = new Type("numeric", 1700, -1); // numeric(precision, decimal), arbitrary precision number
	public static final Type NumericArray = new Type("numeric[]", 1231, -1); //
	public static final Type Numrange = new Type("numrange", 3906, -1); // range of numerics
	public static final Type NumrangeArray = new Type("numrange[]", 3907, -1); //
	// public static final Type Oid = new Type("oid",26,4); // object identifier(oid), maximum 4 billion
	// public static final Type OidArray = new Type("oid[]",1028,-1); //
	// public static final Type Oidvector = new Type("oidvector",30,-1); // array of oids, used in system tables
	// public static final Type OidvectorArray = new Type("oidvector[]",1013,-1); //
	// public static final Type Opaque = new Type("opaque",2282,4); //
	public static final Type Path = new Type("path", 602, -1); // geometric path '(pt1,...)'
	public static final Type PathArray = new Type("path[]", 1019, -1); //
	// public static final Type Pg_Node_Tree = new Type("pg_node_tree",194,-1); // string representing an internal node tree
	public static final Type Point = new Type("point", 600, 16); // geometric point '(x, y)'
	public static final Type PointArray = new Type("point[]", 1017, -1); //
	public static final Type Polygon = new Type("polygon", 604, -1); // geometric polygon '(pt1,...)'
	public static final Type PolygonArray = new Type("polygon[]", 1027, -1); //
	public static final Type Real = new Type("real", 700, 4); // single-precision floating point number, 4-byte storage
	public static final Type RealArray = new Type("real[]", 1021, -1); //
	// public static final Type Record = new Type("record",2249,-1); //
	// public static final Type RecordArray = new Type("record[]",2287,-1); //
	// public static final Type Refcursor = new Type("refcursor",1790,-1); // reference to cursor (portal name)
	// public static final Type RefcursorArray = new Type("refcursor[]",2201,-1); //
	// public static final Type Regclass = new Type("regclass",2205,4); // registered class
	// public static final Type RegclassArray = new Type("regclass[]",2210,-1); //
	// public static final Type Regconfig = new Type("regconfig",3734,4); // registered text search configuration
	// public static final Type RegconfigArray = new Type("regconfig[]",3735,-1); //
	// public static final Type Regdictionary = new Type("regdictionary",3769,4); // registered text search dictionary
	// public static final Type RegdictionaryArray = new Type("regdictionary[]",3770,-1); //
	// public static final Type Regoper = new Type("regoper",2203,4); // registered operator
	// public static final Type RegoperArray = new Type("regoper[]",2208,-1); //
	// public static final Type Regoperator = new Type("regoperator",2204,4); // registered operator (with args)
	// public static final Type RegoperatorArray = new Type("regoperator[]",2209,-1); //
	// public static final Type Regproc = new Type("regproc",24,4); // registered procedure
	// public static final Type RegprocArray = new Type("regproc[]",1008,-1); //
	// public static final Type Regprocedure = new Type("regprocedure",2202,4); // registered procedure (with args)
	// public static final Type RegprocedureArray = new Type("regprocedure[]",2207,-1); //
	// public static final Type Regpublic static final Type = new Type("regtype",2206,4); // registered type
	// public static final Type RegtypeArray = new Type("regtype[]",2211,-1); //
	public static final Type Reltime = new Type("reltime", 703, 4, Converters.intConverter); // relative, limited-range time interval (Unix delta time)
	public static final Type ReltimeArray = new Type("reltime[]", 1024, -1); //
	public static final Type Smallint = new Type("smallint", 21, 2, Converters.shortConverter); // -32 thousand to 32 thousand, 2-byte storage
	public static final Type SmallintArray = new Type("smallint[]", 1005, -1); //
//	public static final Type Smgr = new Type("smgr", 210, 2); // storage manager
	public static final Type Text = new Type("text", 25, -1); // variable-length string, no limit specified
	public static final Type TextArray = new Type("text[]", 1009, -1); //
//	public static final Type Tid = new Type("tid", 27, 6); // (block, offset), physical location of tuple
//	public static final Type TidArray = new Type("tid[]", 1010, -1); //
	public static final Type TimeWithTimeZone = new Type("time with time zone", 1266, 12); // time of day with time zone
	public static final Type TimeWithTimeZoneArray = new Type("time with time zone[]", 1270, -1); //
	public static final Type TimeWithoutTimeZone = new Type("time without time zone", 1083, 8); // time of day
	public static final Type TimeWithoutTimeZoneArray = new Type("time without time zone[]", 1183, -1); //
	public static final Type Time = new Type("time", 1083, 8); // time of day
	public static final Type TimeArray = new Type("_time", 1083, 8); // time of day	
	public static final Type TimestampWithTimeZone = new Type("timestamp with time zone", 1184, 8); // date and time with time zone
	public static final Type TimestampWithTimeZoneArray = new Type("timestamp with time zone[]", 1185, -1); //
	public static final Type TimestampWithoutTimeZone = new Type("timestamp without time zone", 1114, 8); // date and time
	public static final Type TimestampWithoutTimeZoneArray = new Type("timestamp without time zone[]", 1115, -1); //
	public static final Type Timestamp = new Type("timestamp", 1114, 8); // date and time
	public static final Type TimestampArray = new Type("_timestamp", 1115, -1); //
	public static final Type Tinterval = new Type("tinterval", 704, 12); // (abstime,abstime), time interval
	public static final Type TintervalArray = new Type("tinterval[]", 1025, -1); //
//	public static final Type Trigger = new Type("trigger", 2279, 4); //
	public static final Type Tsquery = new Type("tsquery", 3615, -1); // query representation for text search
	public static final Type TsqueryArray = new Type("tsquery[]", 3645, -1); //
	public static final Type Tsrange = new Type("tsrange", 3908, -1); // range of timestamps without time zone
	public static final Type TsrangeArray = new Type("tsrange[]", 3909, -1); //
	public static final Type Tstzrange = new Type("tstzrange", 3910, -1); // range of timestamps with time zone
	public static final Type TstzrangeArray = new Type("tstzrange[]", 3911, -1); //
	public static final Type Tsvector = new Type("tsvector", 3614, -1); // text representation for text search
	public static final Type TsvectorArray = new Type("tsvector[]", 3643, -1); //
	public static final Type Txid_Snapshot = new Type("txid_snapshot", 2970, -1); // txid snapshot
	public static final Type Txid_SnapshotArray = new Type("txid_snapshot[]", 2949, -1); //
	public static final Type Unknown = new Type("unknown", 705, -2); //
	public static final Type Uuid = new Type("uuid", 2950, 16); // UUID datatype
	public static final Type UuidArray = new Type("uuid[]", 2951, -1); //
	public static final Type Void = new Type("void", 2278, 4); //
//	public static final Type Xid = new Type("xid", 28, 4); // transaction id
//	public static final Type XidArray = new Type("xid[]", 1011, -1); //
	public static final Type Xml = new Type("xml", 142, -1); // XML content
	public static final Type XmlArray = new Type("xml[]", 143, -1); //
	
	public static void register(Type type) {
		types.put(type.oid, type);
	}

	public static Type lookup(final int oid) {
		Type t = types.get(oid);
		if (t == null) t = Unknown;
		return t;
	}
}
