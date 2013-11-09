package org.hive2hive.core.model;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * A version of a file in the DHT. A version contains several chunks (depending on the file size and the
 * settings).
 * 
 * @author Nico
 * 
 */
public class FileVersion extends NetworkContent {

	private static final long serialVersionUID = 1L;
	private final int counter;
	private final long size;
	private final long date;
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

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getMetaDocument();
	}
}
