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

	private final String locationCache;

	public UserCredentials(String userId, String password, String pin) {
		this.userId = userId;
		this.password = password;
		this.pin = pin;
		this.locationCache = calculateLocationCache();
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
		return locationCache;
	}

	/**
	 * Calculates the location for this {@link UserCredentials}. Once calculated, the location gets cached and
	 * directly returned on further invokes.
	 * 
	 * @return The location key associated with this credentials.
	 */
	private String calculateLocationCache() {
		// concatenate PIN + PW + UserId
		String appendage = new StringBuilder().append(pin).append(password).append(userId).toString();

		// create fixed salt based on location
		byte[] fixedSalt = PasswordUtil.generateFixedSalt(appendage.getBytes());

		// hash the location
		byte[] locationKey = PasswordUtil.generateHash(appendage.toCharArray(), fixedSalt);

		// Note: Do this as hex to support all platforms
		return EncryptionUtil.byteToHex(locationKey);
	}
}
