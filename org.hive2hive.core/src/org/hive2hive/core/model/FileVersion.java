package org.hive2hive.core.model;

import java.io.Serializable;
import java.util.List;

/**
 * A version of a file in the DHT. A version contains several chunks (depending on the file size and the
 * settings).
 * 
 * @author Nico, Seppi
 */
public class FileVersion implements Serializable, IFileVersion {

	// TODO override equals() and hashCode()
	
	private static final long serialVersionUID = 1L;
	private final int index; // version count
	private final long size; // size of the version
	private final long date; // date when it's created
	private final List<MetaChunk> metaChunks; // the chunk id's to find the chunks

	public FileVersion(int index, long size, long date, List<MetaChunk> metaChunks) {
		this.index = index;
		this.size = size;
		this.date = date;
		this.metaChunks = metaChunks;
	}

	/**
	 * Get a list containing all {@link MetaChunks}. Each meta chunk stores the chunk id and the hash of the
	 * corresponding chunk stored in the network.
	 * 
	 * @return a list with all meta chunks
	 */
	public List<MetaChunk> getMetaChunks() {
		return metaChunks;
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
