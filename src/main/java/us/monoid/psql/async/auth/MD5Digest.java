package us.monoid.psql.async.auth;

/*-------------------------------------------------------------------------
 *
 * Copyright (c) 2003-2011, PostgreSQL Global Development Group
 *
 *
 *-------------------------------------------------------------------------
 */
/**
 * MD5-based utility function to obfuscate passwords before network 
 * transmission.
 *
 * @author Jeremy Wohl
 * @author beders
 */

import java.io.UnsupportedEncodingException;
import java.security.*;

public class MD5Digest {
	static final char lookup[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	private MD5Digest() {
	}

	/**
	 * Encodes user/password/salt information in the following way: MD5(MD5(password + user) + salt)
	 * 
	 * @param aUser The connecting user.
	 * 
	 * @param aPassword The connecting user's password.
	 * 
	 * @param salt A four-salt sent by the server.
	 * 
	 * @return A 35-byte array, comprising the string "md5" and an MD5 digest.
	 */
	public static byte[] encode(String aUser, char[] aPassword, byte salt[]) {
		MessageDigest md;
		byte[] temp_digest, pass_digest;
		byte[] hex_digest = new byte[35];
		byte[] user = toUTF8(aUser);
		byte[] password = toUTF8(new String(aPassword));
		
		try {
			md = MessageDigest.getInstance("MD5");

			md.update(password);
			md.update(user);
			temp_digest = md.digest();

			bytesToHex(temp_digest, hex_digest, 0);
			md.update(hex_digest, 0, 32);
			md.update(salt);
			pass_digest = md.digest();

			bytesToHex(pass_digest, hex_digest, 3);
			hex_digest[0] = (byte) 'm';
			hex_digest[1] = (byte) 'd';
			hex_digest[2] = (byte) '5';
		} catch (Exception e) {
			; // "MessageDigest failure; " + e
		}

		return hex_digest;
	}
	
	private static byte[] toUTF8(String aString) {
		try {
			return aString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		throw new IllegalArgumentException("UTF8 encoding not found");
	}

	/*
	 * Turn 16-byte stream into a human-readable 32-byte hex string
	 */
	private static void bytesToHex(byte[] bytes, byte[] hex, int offset) {
		int pos = offset;

		for (int i = 0; i < 16; i++) {
			int c = bytes[i] & 0xFF;
			int j = c >> 4;
			hex[pos++] = (byte) lookup[j];
			j = (c & 0xF);
			hex[pos++] = (byte) lookup[j];
		}
	}
}
