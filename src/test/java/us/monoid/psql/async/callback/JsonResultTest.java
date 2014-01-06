package us.monoid.psql.async.callback;

import java.io.IOException;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import us.monoid.psql.async.Row;
import us.monoid.psql.async.RowTest;
import us.monoid.psql.async.Transaction;

import static org.junit.Assert.*;

public class JsonResultTest {

	@Test
	public void testArray() throws IOException {
		JsonResult result = new JsonResult(JsonResult.Style.array) {
			@Override
			public void result(JsonObject result, Transaction trx) {
				assertNotNull(result);
				System.out.println(result.encodePrettily());
				assertEquals(result.getArray("string").get(0).toString(),"bubu");
			}
			
		};
		Transaction trx = null; // not needed in this test
		Row row = RowTest.createRow();
		result.start(row.columns(), trx);
		result.row(row, null);
		result.end(1, null);
		
	}

	@Test
	public void testObject() throws IOException {
		JsonResult result = new JsonResult(JsonResult.Style.object) {
			@Override
			public void result(JsonObject result, Transaction trx) {
				assertNotNull(result);
				System.out.println(result.encodePrettily());
				assertEquals(result.getArray("result").<JsonObject>get(0).getString("string"), "bubu");
				assertEquals(result.getArray("result").<JsonObject>get(0).getInteger("int"), new Integer(42)); // auto-boxing fail ;)
			}
			
		};
		Transaction trx = null; // not needed in this test
		Row row = RowTest.createRow();
		result.start(row.columns(), trx);
		result.row(row, null);
		result.end(1, null);
		
	}

	
}
