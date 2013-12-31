package us.monoid.psql.async.message;

import us.monoid.psql.async.Columns;

/** See Postgresl Manual 46.5 Message Formats.
 * 
 * @author beders
 *
 */
public class RowDescription extends BackendMessage {

	public short columnCount() {
		return buffer.getShort(5); // can be 0!
	}
	
	public Columns readColumns() {
		short count = columnCount();

		Columns cs = new Columns(columnCount());
		for (int pos = 7, i = 0; i < count; i++) {
			StringBuilder name = new StringBuilder();
			pos = readCString(pos, name);
			int tableID = buffer.getInt(pos);
			pos+=4;
			short colNumber = buffer.getShort(pos);
			pos+=2;
			int type = buffer.getInt(pos);
			pos+=4;
			short typeSize = buffer.getShort(pos);
			pos+=2;
			int typeModifier = buffer.getInt(pos);
			pos+=4;
			short formatCode = buffer.getShort(pos);
			pos+=2;
			
			cs.setColumn(i, name.toString(), type, formatCode);
		}
		return cs;
	}

}
