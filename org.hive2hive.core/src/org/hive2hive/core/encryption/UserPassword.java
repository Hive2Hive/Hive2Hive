package org.hive2hive.core.encryption;

/**
 * This class simply represents a user password. Do not change the password or the PIN manually by using
 * setters but rather use PasswordUtil.generatePassword() to generate a new user password. The PIN needs to be
 * unique per-user per-password.
 * 
 * @author Christian
 * 
 */
public final class UserPassword {

	private char[] password;
	private char[] pin;

	public UserPassword(char[] password, char[] pin) {

		this.password = password;
		this.pin = pin;
	}

	public char[] getPassword() {
		return password;
	}

	public char[] getPin() {
		return pin;
	}
}
