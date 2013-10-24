package org.hive2hive.core.encryption;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class ProfileEncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ProfileEncryptionUtil.class);

	private ProfileEncryptionUtil() {
	}

	/**
	 * Creates the UserPassword based on the password set by the user.
	 * @param password
	 * @return
	 */
	public static UserPassword createUserPassword(String password){
		
		char[] pwAsChar = password.toCharArray();
		byte[] randomSalt = EncryptionUtil.createSalt(8);
		return new UserPassword(pwAsChar, randomSalt);
	}
}
