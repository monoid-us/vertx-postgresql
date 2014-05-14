package us.monoid.psql.async.message;

public class BackendKeyData extends BackendMessage {

	public int getProcessID() {
		return buffer.getInt(5);
	}
	
	public int getSecretKey() {
		return buffer.getInt(9);
	}
}
