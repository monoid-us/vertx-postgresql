package us.monoid.psql.async;

import static org.junit.Assert.*;

import org.junit.Test;

public class ColumnsTest {

	@Test
	public void test() {
		Columns c = new Columns((short)3);
		c.setColumn(0, "string", 1043, (short)0); // varchar
		c.setColumn(1, "int", 23, (short)0); // integer
		c.setColumn(2, "long", 20, (short)0); // long
		
		assertEquals(c.get(0).name, "string");
		assertEquals(c.get("string"), c.get(0));
		assertEquals(c.get(c.index("string")), c.get("string"));
	}

}
