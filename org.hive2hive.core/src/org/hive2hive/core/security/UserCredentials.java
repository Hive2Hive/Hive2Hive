package org.hive2hive.core.security;

/**
 * This stores a user's credentials. Do not change the password or the PIN manually by using
 * setters but rather define both parameters from scratch. The PIN needs to be unique per-user per-password.
 * 
 * @author Christian
 * 
 */
public final class UserCredentials {
	
	private final String userId;
	private final String password;
	private final String pin;

	public UserCredentials(String userId, String password, String pin) {
		this.userId = userId;
		this.password = password;
		this.pin = pin;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public String getPin() {
		return pin;
	}
}