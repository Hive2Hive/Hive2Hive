package org.hive2hive.core.model;

import java.io.Serializable;
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
public class FileVersion implements Serializable {

	private static final long serialVersionUID = 1L;
	private final int counter; // version count
	private final long size; // size of the version
	private final long date; // date when it's created
	private List<KeyPair> chunkIds;

	public FileVersion(int counter, long size, long date) {
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

	public long getSize() {
		return size;
	}

	public long getDate() {
		return date;
	}
}
