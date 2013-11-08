package org.hive2hive.core;

public class UserCredentials {
	private String userId;
	private String password;
	private String pin;

	public UserCredentials(String userId, String password, String pin) {
		this.userId = userId;
		this.password = password;
		this.pin = pin;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}
}