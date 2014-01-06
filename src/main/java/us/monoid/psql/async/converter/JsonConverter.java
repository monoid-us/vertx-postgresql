package us.monoid.psql.async.converter;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/** Converter to support the json type in Postgres.
 * 
 * Will create a Vertx JsonObject when converted to an Object (Row.asObject())
 * If you still want the raw JSON, use Row.asString(...)
 * Note: Json objects are expected as result. If a Json array is passed, we'll still try to parse it as object.
 * If that fails, we parse it as JsonArray.
 * 
 * @author beders
 *
 */
public class JsonConverter extends StringConverter {
	@Override
	public Object toObject(Buffer buffer, int pos, int len, boolean isBinary) {
		Object jsonObject;
		String json = toString(buffer,pos,len,isBinary);
		try {
			jsonObject = new JsonObject(json); // would love to avoid doing the string conversion
		} catch (DecodeException de) {
			jsonObject = new JsonArray(json);
		}
		return jsonObject;
	}
}
