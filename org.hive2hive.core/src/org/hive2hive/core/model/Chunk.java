package org.hive2hive.core.model;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * Raw data part of a file that is added to the DHT
 * 
 * @author Nico
 */
public class Chunk extends NetworkContent {

	private static final long serialVersionUID = 1L;

	private final String id;
	private final int size;
	private final byte[] data;
	private final int order;

	public Chunk(String id, byte[] data, int order, int size) {
		this.id = id;
		this.data = data;
		this.order = order;
		this.size = size;
	}

	public int getSize() {
		return size;
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
