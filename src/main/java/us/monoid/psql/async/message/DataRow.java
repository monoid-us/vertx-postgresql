package us.monoid.psql.async.message;

import org.vertx.java.core.buffer.Buffer;


public class DataRow extends BackendMessage {
	// this is possibly a micro-optimization, as finding the data for a column is summing up a few ints
	// we are sacrificing heap for ease-of-use and cpu
	int[] indexes; // for each column, records pointer in buffer where the content is. Makes it easier to read column data which will most likely be done
	
	@Override 
	public void setBuffer(Buffer aBuffer) {
		super.setBuffer(aBuffer);
		short col = buffer.getShort(5);
		indexes = new int[col];
		for (int pos = 7,i = 0; i < col; i++) {
			indexes[i] = pos;
			int lenData = buffer.getInt(pos); // lenght is raw length of column data excluding the length information
			if (lenData == -1) { // NULL value
				pos += 4; // next column starts right away
			} else {
				pos += 4 + lenData;
			}
		}
	}
	
	/** Get a copy of the raw bytes making up the value of that column. Data will be copied for each call to this method.
	 * 
	 * @param column the column index (0-based)
	 * @return a buffer with the raw data
	 */
	public Buffer getRawBytes(int column) {
		int lenData = buffer.getInt(indexes[column]);
		if (lenData == -1) return new Buffer(0); // NULL
		return buffer.getBuffer(indexes[column] + 4,  indexes[column] + 4 + lenData);		
	}

	public Buffer getBuffer() {
		return buffer;
	}

	/** Return the length of the data in the column. If -1, there is no data -> null */
	public int len(int col) {
		return buffer.getInt(indexes[col]);
	}

	/** Return the position in the buffer where the data starts. If -1, there is no data -> null */
	public int pos(int col) {
		return indexes[col] + 4;
	}
	
}






