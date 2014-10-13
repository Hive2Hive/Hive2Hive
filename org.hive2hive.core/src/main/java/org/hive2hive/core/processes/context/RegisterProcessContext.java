package org.hive2hive.core.processes.context;

import java.security.KeyPair;

import javax.crypto.SecretKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.security.PasswordUtil;
import org.hive2hive.core.security.UserCredentials;

/**
 * @author Nico, Seppi
 */
public final class RegisterProcessContext {

	private final UserCredentials userCredentials;

	private Locations locations;
	private UserProfile profile;

	public RegisterProcessContext(UserCredentials userCredentials) {
		this.userCredentials = userCredentials;
	}

	public String consumeUserId() {
		return userCredentials.getUserId();
	}

	public String consumeUserProflieLocationKey() {
		return userCredentials.getProfileLocationKey();
	}

	public void provideUserLocations(Locations locations) {
		this.locations = locations;
	}

	public Locations consumeUserLocations() {
		return locations;
	}

	public void provideUserProfile(UserProfile profile) {
		this.profile = profile;
	}

	public UserProfile consumeUserProfile() {
		return profile;
	}

	public KeyPair consumeUserLocationsProtectionKeys() {
		return profile.getProtectionKeys();
	}

	public KeyPair consumeUserProfileProtectionKeys() {
		return profile.getProtectionKeys();
	}

	public KeyPair consumeUserPublicKeyProtectionKeys() {
		return profile.getProtectionKeys();
	}

	public SecretKey consumeUserProfileEncryptionKeys() {
		return PasswordUtil.generateAESKeyFromPassword(userCredentials.getPassword(), userCredentials.getPin(),
				H2HConstants.KEYLENGTH_USER_PROFILE);
	}

}
