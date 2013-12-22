package us.monoid.psql.async.message;


public class AuthenticationRequest extends BackendMessage {

	private int authInfo() {
		return buffer.getInt(5);
	}
	
	/** Retrieve the salt if this is MD5 password authentication request. Will throw exception if you try to call this and needMD5Password returns false */
	public byte[] salt() {
		return buffer.getBytes(9, 13);
	}
	
	public boolean isAuthenticationOk() {
		return authInfo() == 0;
	}

	public boolean needCleartextPassword() {
		return authInfo() == 3;
	}
	
	public boolean needMD5Password() {
		return authInfo() == 5;
	}
	
	public boolean isAuthenticationSupported() {
		return needCleartextPassword() || needMD5Password();
	}
}
