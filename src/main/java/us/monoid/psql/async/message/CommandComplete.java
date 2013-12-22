package us.monoid.psql.async.message;

public class CommandComplete extends BackendMessage {

	public String getTag() {
		return readCString(5);
	}

}
