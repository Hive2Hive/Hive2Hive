package org.hive2hive.core.model;

import org.hive2hive.core.TimeToLiveStore;

/**
 * Raw data part of a file that is added to the DHT
 * 
 * @author Nico
 */
public class Chunk extends NetworkContent {

	private static final long serialVersionUID = 6880686784324242531L;

	private final String id;
	private final byte[] data;
	private final int order;

	public Chunk(String id, byte[] data, int order) {
		this.id = id;
		this.data = data;
		this.order = order;
	}

	public int getSize() {
		return data.length;
	}

	public String getId() {
		return id;
	}

	public byte[] getData() {
		return data;
	}

	public int getOrder() {
		return order;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getChunk();
	}
}
