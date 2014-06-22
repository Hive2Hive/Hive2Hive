package org.hive2hive.core.model;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Holds meta data of a small file in the DHT
 * 
 * @author Nico, Seppi
 */
public class MetaFileSmall extends MetaFile {

	private static final long serialVersionUID = -3385321499412137545L;
	private final List<FileVersion> versions;
	private final KeyPair chunkKey;

	public MetaFileSmall(PublicKey id, List<FileVersion> versions, KeyPair chunkKey) {
		super(id, true);
		this.versions = versions;
		this.chunkKey = chunkKey;
	}

	public List<FileVersion> getVersions() {
		return versions;
	}

	public KeyPair getChunkKey() {
		return chunkKey;
	}

	public BigInteger getTotalSize() {
		if (versions == null) {
			return BigInteger.ZERO;
		} else {
			BigInteger sum = BigInteger.ZERO;
			for (FileVersion version : versions) {
				sum = sum.add(version.getSize());
			}
			return sum;
		}
	}

	public FileVersion getNewestVersion() {
		if (versions == null || versions.isEmpty()) {
			return null;
		}

		Collections.sort(versions, new Comparator<FileVersion>() {
			@Override
			public int compare(FileVersion o1, FileVersion o2) {
				return new Integer(o1.getIndex()).compareTo(o2.getIndex());
			}
		});

		return versions.get(versions.size() - 1);
	}

	public FileVersion getVersionByIndex(int index) {
		if (versions == null || versions.isEmpty()) {
			return null;
		}

		for (FileVersion version : versions) {
			if (version.getIndex() == index) {
				return version;
			}
		}

		return null;
	}
}
