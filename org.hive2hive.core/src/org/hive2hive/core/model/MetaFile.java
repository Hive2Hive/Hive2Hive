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

	private static final long serialVersionUID = 1L;
	private List<FileVersion> versions;

	public MetaFile(PublicKey id) {
		super(id);
		setVersions(new ArrayList<FileVersion>());
	}

	public List<FileVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<FileVersion> versions) {
		this.versions = versions;
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
				return new Integer(o1.getCounter()).compareTo(o2.getCounter());
			}
		});

		return versions.get(0);
	}
}
