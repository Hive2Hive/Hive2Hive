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

	private final char[] password;
	private final char[] pin;
	private final String locationKey;

	public UserPassword(char[] password, char[] pin) {

		this.password = password;
		this.pin = pin;
		this.locationKey = calculateLocationKey();
	}

	private String calculateLocationKey() {
		
		// concatenate PIN + PW
		char[] location = new StringBuilder().append(pin).append(password).toString().toCharArray();
		
		// create fixed salt based on location
		byte[] fixedSalt = PasswordUtil.generateFixedSalt(EncryptionUtil.serializeObject(location));
		
		
		// hash the location
		byte[] locationKey = PasswordUtil.generateHash(location, fixedSalt);
		
		return locationKey.toString();
	}
	
	public char[] getPassword() {
		return password;
	}

	public char[] getPin() {
		return pin;
	}
	
	public String getLocationKey() {
		return locationKey;
	}
}
