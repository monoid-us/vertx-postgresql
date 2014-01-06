package us.monoid.psql.async.converter;

import static org.junit.Assert.*;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class JsonConverterTest {

	@Test
	public void test() {
		Buffer buffer = StringConverterTest.createBufferForString("{\"name\": \"Adams\", \"age\": 42}");
		Object obj = Converters.jsonConverter.toObject(buffer, 0, buffer.length(), false);
		assertTrue(obj instanceof JsonObject);
		JsonObject json = (JsonObject)obj;
		System.out.println(json.encodePrettily());
		assertEquals(json.getString("name"), "Adams");
		
		buffer = StringConverterTest.createBufferForString("[{\"name\": \"Adams\", \"age\": 42}]");
		obj = Converters.jsonConverter.toObject(buffer, 0, buffer.length(), false);
		assertTrue(obj instanceof JsonArray);
		JsonArray arr = (JsonArray)obj;
		System.out.println(arr.encodePrettily());
		assertEquals(arr.<JsonObject>get(0).getString("name"), "Adams");
	}

}
