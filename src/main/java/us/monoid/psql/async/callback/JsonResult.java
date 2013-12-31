package us.monoid.psql.async.callback;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import us.monoid.psql.async.Columns;
import us.monoid.psql.async.Row;
import us.monoid.psql.async.Transaction;

/** ResultListener which creates a Json Object from the query result.
 * Use this if you want a quick conversion of rows into JSON.
 * 
 * Usage:
 * {@code trx.query(sql, new JsonResult(JsonResult.Style.array) { 
 * 	public void result(JsonObject result, Transaction trx) {
 * 		...
 * }
 * }
 * } 
 * 
 * Column names will be converted to lower-case and used as keys for the row values.
 * There are two styles available.
 * <ul><li>array - the json object created will have an array for each column, values are in order of the query result
 * Example: { name: ['Hans', 'Armin' ], age: [44, 43] }
 * <li>object - each row is an object in an array for the key 'result'
 * Example: { result: [ { name: 'Hans', age: 44 }, { name: 'Armin', age: 43 }]} 
 * </ul>
 * 
 * Additionally you can specify custom converters for each column (only one converter per column)
 * @see JsonResult.Converter
 * 
 * @author beders
 *
 */
public abstract class JsonResult implements ResultListener {
	enum Style { array, object };
	
	JsonObject object = new JsonObject();
	JsonArray results;
	JsonArray[] columns;
	Style style;
	Converter[] converters;
	Converter[] columnConverter;
	
	/** Turn query result into a JsonObject using the specified style and optional converters */
	public JsonResult(Style aStyle, Converter... someConverters) {
		style = aStyle;
		converters = someConverters;
	}
	
	@Override
	public void start(Columns cols, Transaction trx) {
		columnConverter = new Converter[cols.count()];
		for (Converter c : converters) { // set a converter for the request columns
			columnConverter[cols.index(c.columnName)] = c;
		}
		switch (style) {
		case object:
			results = new JsonArray();
			object.putArray("result", results);
			break;
		case array:
			columns = new JsonArray[cols.count()];
			for (int i = 0; i < cols.count(); i++) {
				columns[i] = new JsonArray();
				object.putArray(cols.get(i).name.toLowerCase(), columns[i]);
			}
			break;
		}
	}

	@Override
	public void row(Row row, Transaction trx) {
		switch (style) {
		case object:
			JsonObject jRow = new JsonObject();
			for (int i = 0; i < row.columns().count(); i++) {
				jRow.putValue(row.columns().get(i).name.toLowerCase(), 
						columnConverter[i] != null ? columnConverter[i].convert(row.asObject(i)) : row.asObject(i));
			}
			results.addObject(jRow);
			break;
		case array:
			for (int i = 0; i < row.columns().count(); i++) {
				columns[i].add(columnConverter[i] != null ? columnConverter[i].convert(row.asObject(i)) : row.asObject(i));
			}
			break;
		}
	}

	@Override
	public void end(int count, Transaction trx) {
		result(object, trx);
	}
	
	public abstract void result(JsonObject result, Transaction trx);
	
	/** Create a subclass of this to use as a custom converter for a column in the result set.
	 * You need to specify the column name in the result.
	 * Implement convert(Object):Object which will pass in the value of a column as Object and sets the returned value in the resulting Json.
	 * @author beders
	 *
	 */
	public static abstract class Converter {
		String columnName;
		public Converter(String aColumnName) {
			columnName = aColumnName;
		}
		public abstract Object convert(Object value); 
	}
}
