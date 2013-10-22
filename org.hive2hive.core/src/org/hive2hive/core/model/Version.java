package org.hive2hive.core.model;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

/**
 * A version of a file in the DHT. A version contains several chunks (depending on the file size and the
 * settings).
 * 
 * @author Nico
 * 
 */
public class Version {

	private final int counter;
	private final int size;
	private final long date;
	private List<KeyPair> chunkIds;

	public Version(int counter, int size, long date) {
		this.counter = counter;
		this.size = size;
		this.date = date;
		chunkIds = new ArrayList<KeyPair>();
	}

	public List<KeyPair> getChunkIds() {
		return chunkIds;
	}

	public void setChunkIds(List<KeyPair> chunkIds) {
		this.chunkIds = chunkIds;
	}

	public int getCounter() {
		return counter;
	}

	public int getSize() {
		return size;
	}

	public long getDate() {
		return date;
	}
}
