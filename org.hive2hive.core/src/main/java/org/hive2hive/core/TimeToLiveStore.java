package org.hive2hive.core;

/**
 * Stores the configured time to live values (seconds)
 * 
 * @author Nico
 * 
 */
public class TimeToLiveStore {

	private int chunk = convertDaysToSeconds(180);
	private int metaDocument = convertDaysToSeconds(365);
	private int userProfile = convertDaysToSeconds(2 * 365);
	private int locations = convertDaysToSeconds(2 * 365);
	private int userMessageQueue = convertDaysToSeconds(2 * 365);

	private static final int DAY_SECOND_FACTOR = 24 * 60 * 60;
	private static TimeToLiveStore instance;

	public static TimeToLiveStore getInstance() {
		if (instance == null) {
			instance = new TimeToLiveStore();
		}

		return instance;
	}

	public int getChunk() {
		return chunk;
	}

	public void setChunk(int chunk) {
		this.chunk = chunk;
	}

	public int getMetaFile() {
		return metaDocument;
	}

	public void setMetaDocument(int metaDocument) {
		this.metaDocument = metaDocument;
	}

	public int getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(int userProfile) {
		this.userProfile = userProfile;
	}

	public int getLocations() {
		return locations;
	}

	public void setLocations(int locations) {
		this.locations = locations;
	}

	public int getUserMessageQueue() {
		return userMessageQueue;
	}

	public void setUserMessageQueue(int userMessageQueue) {
		this.userMessageQueue = userMessageQueue;
	}

	/**
	 * Converts an integer number of days to seconds (which are used by the
	 * {@link TimeToLiveStore}). If an Integer overflow happens,
	 * {@link Integer#MAX_VALUE} is returned.
	 * 
	 * @param days
	 *            the number of days
	 * @return seconds the number of seconds
	 */
	public static int convertDaysToSeconds(int days) {
		assert days > 0;
		if (DAY_SECOND_FACTOR > Long.MAX_VALUE / days) {
			// it will overflow, return the max value
			return Integer.MAX_VALUE;
		}
		return days * DAY_SECOND_FACTOR;
	}
}
