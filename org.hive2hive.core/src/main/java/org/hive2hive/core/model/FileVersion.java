package org.hive2hive.core.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * A version of a file in the DHT. A version contains several chunks (depending on the file size and the
 * settings).
 * 
 * @author Nico, Seppi
 */
public class FileVersion implements Serializable, IFileVersion {

	private static final long serialVersionUID = -3475882940245139367L;

	private final int index; // version count
	private final BigInteger size; // size of the version in bytes
	private final long date; // date when it's created
	private final List<MetaChunk> metaChunks; // the chunk id's to find the chunks

	public FileVersion(int index, long size, long date, List<MetaChunk> metaChunks) {
		this(index, BigInteger.valueOf(size), date, metaChunks);
	}

	public FileVersion(int index, BigInteger size, long date, List<MetaChunk> metaChunks) {
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
	public BigInteger getSize() {
		return size;
	}

	@Override
	public long getDate() {
		return date;
	}

	@Override
	public String toString() {
		return String.format("Version %s [%s] (%s Bytes)", index, DateFormat.getDateTimeInstance().format(new Date(date)),
				size);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (!(obj instanceof FileVersion)) {
			return false;
		}

		FileVersion fv = (FileVersion) obj;
		return fv.index == index && fv.date == date && fv.size == size && fv.metaChunks.size() == metaChunks.size();
	}

	@Override
	public int hashCode() {
		return index;
	}
}
