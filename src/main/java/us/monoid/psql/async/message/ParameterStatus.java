package us.monoid.psql.async.message;

import java.util.Map;

import org.vertx.java.core.buffer.Buffer;

public class ParameterStatus extends BackendMessage {
	int pos;
	
	@Override
	public void setBuffer(Buffer aBuffer) {
		super.setBuffer(aBuffer);
		pos = 0;
	}

	public void addToMap(Map<String,String> params) {
		pos = 5;
		String key = nextCString();
		String value = nextCString();
		params.put(key, value);
	}

	private String nextCString() {
		StringBuilder sb = new StringBuilder();
		pos = readCString(pos, sb);
		return sb.toString();
	}
}
