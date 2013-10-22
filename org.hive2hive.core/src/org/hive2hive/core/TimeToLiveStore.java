package org.hive2hive.core;

/**
 * Stores the configured time to live values (seconds)
 * 
 * @author Nico
 * 
 */
public class TimeToLiveStore {

	public final int chunk;
	public final int metaDocument;
	public final int userDocument;
	public final int locations;
	public final int userMessageQueue;

	public TimeToLiveStore(int chunk, int metaDocument, int userDocument, int locations, int userMessageQueue) {
		this.chunk = chunk;
		this.metaDocument = metaDocument;
		this.userDocument = userDocument;
		this.locations = locations;
		this.userMessageQueue = userMessageQueue;
	}
}
