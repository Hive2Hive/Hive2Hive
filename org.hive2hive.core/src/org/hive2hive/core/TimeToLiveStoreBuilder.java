package org.hive2hive.core;

/**
 * Builder for configuring the time to live for each data in the network
 * Note that all values are in seconds
 * 
 * @author Nico
 * 
 */
public class TimeToLiveStoreBuilder {

	private int chunk = 180 * 24 * 60 * 60;
	private int metaDocument = 365 * 24 * 60 * 60;
	private int userDocument = 2 * 365 * 24 * 60 * 60;
	private int locations = 2 * 365 * 24 * 60 * 60;
	private int userMessageQueue = 2 * 365 * 24 * 60 * 60;

	public int getChunk() {
		return chunk;
	}

	public TimeToLiveStoreBuilder setChunk(int chunk) {
		this.chunk = chunk;
		return this;
	}

	public int getMetaDocument() {
		return metaDocument;
	}

	public TimeToLiveStoreBuilder setMetaDocument(int metaDocument) {
		this.metaDocument = metaDocument;
		return this;
	}

	public int getUserDocument() {
		return userDocument;
	}

	public TimeToLiveStoreBuilder setUserDocument(int userDocument) {
		this.userDocument = userDocument;
		return this;
	}

	public int getLocations() {
		return locations;
	}

	public TimeToLiveStoreBuilder setLocations(int locations) {
		this.locations = locations;
		return this;
	}

	public int getUserMessageQueue() {
		return userMessageQueue;
	}

	public TimeToLiveStoreBuilder setUserMessageQueue(int userMessageQueue) {
		this.userMessageQueue = userMessageQueue;
		return this;
	}

	public static int convertDaysToSeconds(int days) {
		return days * 24 * 60 * 60;
	}

	public TimeToLiveStore build() {
		return new TimeToLiveStore(chunk, metaDocument, userDocument, locations, userMessageQueue);
	}
}
