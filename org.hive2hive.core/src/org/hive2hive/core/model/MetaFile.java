package org.hive2hive.core.model;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Holds meta data of a file in the DHT
 * 
 * @author Nico
 * 
 */
public class MetaFile extends MetaDocument {

	private List<Version> versions;

	public MetaFile(PublicKey id) {
		super(id);
		setVersions(new ArrayList<Version>());
	}

	public List<Version> getVersions() {
		return versions;
	}

	public void setVersions(List<Version> versions) {
		this.versions = versions;
	}

	public int getTotalSize() {
		if (versions == null) {
			return 0;
		} else {
			int sum = 0;
			for (Version version : versions) {
				sum += version.getSize();
			}
			return sum;
		}
	}

	public Version getNewestVersion() {
		if (versions == null || versions.isEmpty()) {
			return null;
		}

		Collections.sort(versions, new Comparator<Version>() {
			@Override
			public int compare(Version o1, Version o2) {
				return new Integer(o1.getCounter()).compareTo(o2.getCounter());
			}
		});

		return versions.get(0);
	}
}
