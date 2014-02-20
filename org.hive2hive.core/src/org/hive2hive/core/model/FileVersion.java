package org.hive2hive.core.model;

import java.io.Serializable;
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
	private final List<String> chunkIds; // the chunk id's to find the chunks

	public FileVersion(int index, long size, long date, List<String> chunkIds) {
		this.index = index;
		this.size = size;
		this.date = date;
		this.chunkIds = chunkIds;
	}

	public List<String> getChunkIds() {
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
