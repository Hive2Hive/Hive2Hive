package org.hive2hive.core.model;

import java.security.PublicKey;

import org.hive2hive.core.network.data.BaseDataWrapper;

/**
 * Raw data part of a file that is added to the DHT
 * 
 * @author Nico
 * 
 */
public class Chunk extends BaseDataWrapper {

	private static final long serialVersionUID = 1L;

	private final int size;
	private final PublicKey id;
	private final byte[] data;
	private final int order;

	public Chunk(PublicKey id, byte[] data, int order, int size) {
		this.id = id;
		this.data = data;
		this.order = order;
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public PublicKey getId() {
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
		// TODO Auto-generated method stub
		return 0;
	}
}
