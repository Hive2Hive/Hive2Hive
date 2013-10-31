package org.hive2hive.core.security;

/**
 * This class simply represents a user password. Do not change the password or the PIN manually by using
 * setters but rather use PasswordUtil.generatePassword() to generate a new user password. The PIN needs to be
 * unique per-user per-password.
 * 
 * @author Christian
 * 
 */
public final class UserPassword {

	private final String password;
	private final String pin;

	public UserPassword(String password, String pin) {
		this.password = password;
		this.pin = pin;
	}

	public String getPassword() {
		return password;
	}

	public String getPin() {
		return pin;
	}
}
