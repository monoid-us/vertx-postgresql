package us.monoid.psql.async.message;

/** Backend message for EmptyQueryResponse 'I':
 * 
 * @author beders
 *
 */
public class EmptyQueryResponse extends BackendMessage {

	public void read() {
		messageLength();
	}

}
