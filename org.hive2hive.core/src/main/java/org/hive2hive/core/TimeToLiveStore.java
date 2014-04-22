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
	 * Converts an integer number of days to seconds (which are used by the {@link TimeToLiveStore}
	 * 
	 * @param days
	 * @return seconds
	 */
	public static int convertDaysToSeconds(int days) {
		return days * 24 * 60 * 60;
	}
}
