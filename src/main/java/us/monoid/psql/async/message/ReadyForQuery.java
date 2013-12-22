package us.monoid.psql.async.message;


public class ReadyForQuery extends BackendMessage {
	
	public boolean isIdle() {
		return buffer.getByte(6) == 'I';
	}

}
