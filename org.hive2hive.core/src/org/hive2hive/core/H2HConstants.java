package org.hive2hive.core;

public interface H2HConstants {

	// standard port for the hive2hive network
	public static final int H2H_PORT = 4622;

	// configurations for network messages
	public static final int MAX_MESSAGE_SENDING = 5;

	// maximal numbers of versions kept in the DHT (see versionKey)
	public static final int MAX_VERSIONS_HISTORY = 5;
	public static final long MIN_VERSIONS_AGE_BEFORE_REMOVAL_MS = 5 * 60 * 1000; // 5mins

	// DHT content keys - these are used to distinguish the different types data
	// stored for a given key
	public static final String USER_PROFILE = "USER_PROFILE";
	public static final String USER_LOCATIONS = "USER_LOCATIONS";
	public static final String USER_PUBLIC_KEY = "USER_PUBLIC_KEY";

}
