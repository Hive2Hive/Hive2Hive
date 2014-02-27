package org.hive2hive.core.model;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * Holds meta data of a file in the DHT
 * 
 * @author Nico
 * 
 */
public class MetaFile extends NetworkContent {

	private static final long serialVersionUID = -3385321499412137545L;
	private final PublicKey id;
	private final List<FileVersion> versions;
	private final KeyPair chunkKey;

	public MetaFile(PublicKey id, List<FileVersion> versions, KeyPair chunkKey) {
		this.id = id;
		this.versions = versions;
		this.chunkKey = chunkKey;
	}

	public PublicKey getId() {
		return id;
	}

	public List<FileVersion> getVersions() {
		return versions;
	}

	public KeyPair getChunkKey() {
		return chunkKey;
	}

	public int getTotalSize() {
		if (versions == null) {
			return 0;
		} else {
			int sum = 0;
			for (FileVersion version : versions) {
				sum += version.getSize();
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
			if (version.getIndex() == index)
				return version;
		}

		return null;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getMetaFile();
	}
}
