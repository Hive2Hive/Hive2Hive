package org.hive2hive.core;

public interface H2HConstants {

	// standard port for the hive2hive network
	public static final int H2H_PORT = 4622;

	// configurations for network messages
	public static final int MAX_MESSAGE_SENDING = 5;

	// DHT content keys - these are used to distinguish the different types data
	// stored for a given key
	public static final String USER_PROFILE = "USER_PROFILE";
	public static final String USER_LOCATIONS = "USER_LOCATIONS";
	public static final String USER_PUBLIC_KEY = "USER_PUBLIC_KEY";

	// waiting time (in milliseconds) after a put operation to verify if put succeeded
	public static final long PUT_VERIFICATION_WAITING_TIME = 2000;

	// number of allowed tries to retry a put
	public static final int PUT_RETRIES = 3;
	// number of allowed tries to get for verification a put
	public static final int GET_RETRIES = 3;

}
