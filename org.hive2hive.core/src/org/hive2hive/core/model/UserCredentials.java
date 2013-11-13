package org.hive2hive.core.model;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.security.PasswordUtil;

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

	public String getProfileLocationKey() {
		// concatenate PIN + PW + UserId
		String location = new StringBuilder().append(pin).append(password).append(userId).toString();

		// create fixed salt based on location
		byte[] fixedSalt = PasswordUtil.generateFixedSalt(location.getBytes());

		// hash the location
		byte[] locationKey = PasswordUtil.generateHash(location.toCharArray(), fixedSalt);

		return new String(locationKey, H2HConstants.ENCODING_CHARSET);
	}
}