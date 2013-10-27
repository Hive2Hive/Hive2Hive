package org.hive2hive.core.encryption;

/**
 * This class simply represents a user password. Each instance automatically generates a new salt as salts
 * should be unique per-user per-password.
 * 
 * @author Christian
 * 
 */
public final class UserPassword {

	private char[] password;
	private byte[] salt;

	public UserPassword(char[] password) {

		this.password = password;
		this.salt = PasswordUtil.generateSalt();
	}

	public char[] getPassword() {
		return password;
	}

	public byte[] getSalt() {
		return salt;
	}
}
