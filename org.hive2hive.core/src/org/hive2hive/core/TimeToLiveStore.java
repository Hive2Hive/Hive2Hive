package org.hive2hive.core;

/**
 * Stores the configured time to live values (seconds)
 * 
 * @author Nico
 * 
 */
public class TimeToLiveStore {

	private int chunk;
	private int metaDocument;
	private int userProfile;
	private int locations;
	private int userMessageQueue;

	private static class SingletonHolder {
		private static final TimeToLiveStore INSTANCE = new TimeToLiveStore();

		private SingletonHolder() {
			INSTANCE.setChunk(convertDaysToSeconds(180));
			INSTANCE.setMetaDocument(convertDaysToSeconds(365));
			INSTANCE.setUserProfile(convertDaysToSeconds(2 * 365));
			INSTANCE.setLocations(convertDaysToSeconds(2 * 365));
			INSTANCE.setUserMessageQueue(convertDaysToSeconds(2 * 365));
		}
	}

	public static TimeToLiveStore getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public int getChunk() {
		return chunk;
	}

	public void setChunk(int chunk) {
		this.chunk = chunk;
	}

	public int getMetaDocument() {
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
