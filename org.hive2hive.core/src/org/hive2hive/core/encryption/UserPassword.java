package org.hive2hive.core.encryption;

/**
 * This class simply represents a user password and its associated salt.
 * @author Christian
 *
 */
public final class UserPassword {

	private char[] password;
	private byte[] salt;
	
	public UserPassword(char[] password, byte[] salt){
		this.password = password;
		this.salt = salt;
	}
	
	public char[] getPassword() {
		return password;
	}
	
	public byte[] getSalt() {
		return salt;
	}
}
