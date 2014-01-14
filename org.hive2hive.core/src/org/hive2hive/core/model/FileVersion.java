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
public class FileVersion implements Serializable, IFileVersion {

	private static final long serialVersionUID = 1L;
	private final int index; // version count
	private final long size; // size of the version
	private final long date; // date when it's created
	private List<KeyPair> chunkIds;

	public FileVersion(int index, long size, long date) {
		this.index = index;
		this.size = size;
		this.date = date;
		chunkIds = new ArrayList<KeyPair>();
	}

	public List<KeyPair> getChunkIds() {
		return chunkIds;
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
