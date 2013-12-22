package us.monoid.psql.async;

import us.monoid.psql.async.converter.Converter;
import us.monoid.psql.async.converter.Converters;

/** Description of a data type.
 * See Types for a list of known types. 
 * You can create your own user types as well. (later) TODO
 * @author beders
 *
 */
public class Type {
	final String name;
	final int oid; // internal identifier in the pg_type table and part of the RowDescription server message
	final int size;
	final Converter converter;
	
	public Type(String aName, int anOid, int aSize, Converter aConverter) {
		name = aName;
		oid = anOid;
		size = aSize;
		converter = aConverter;
		Types.register(this);

	}
	
	public Type(String aName, int anOid, int aSize) {
		this(aName, anOid, aSize, Converters.stringConverter);
	}
	
	
}
