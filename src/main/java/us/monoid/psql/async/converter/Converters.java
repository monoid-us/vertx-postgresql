package us.monoid.psql.async.converter;

/** List of all known converters.
 * TODO - waaaay more converters
 **/
public class Converters {

	public static final StringConverter stringConverter = new StringConverter();
	public static final NumberConverter shortConverter = new NumberConverter(2);
	public static final NumberConverter intConverter = new NumberConverter(4);
	public static final NumberConverter longConverter = new NumberConverter(8);
	

}
