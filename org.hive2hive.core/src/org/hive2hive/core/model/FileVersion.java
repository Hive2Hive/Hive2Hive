package org.hive2hive.core.model;

import java.io.Serializable;
import java.security.KeyPair;
import java.util.List;

/**
 * A version of a file in the DHT. A version contains several chunks (depending on the file size and the
 * settings).
 * 
 * @author Nico
 * 
 */
public class FileVersion implements Serializable, IFileVersion {

	private static final long serialVersionUID = 1L;
	private final int index; // version count
	private final long size; // size of the version
	private final long date; // date when it's created
	private final List<KeyPair> chunkKeys; // the chunk keys to find and decrypt chunks

	public FileVersion(int index, long size, long date, List<KeyPair> chunkKeys) {
		this.index = index;
		this.size = size;
		this.date = date;
		this.chunkKeys = chunkKeys;
	}

	public List<KeyPair> getChunkKeys() {
		return chunkKeys;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public long getDate() {
		return date;
	}
}
