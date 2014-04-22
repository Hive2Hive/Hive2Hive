package org.hive2hive.core.model;

import java.security.PublicKey;
import java.util.List;

/**
 * Holds meta data of a large file in the DHT
 * 
 * @author Nico
 */
public class MetaFileLarge extends MetaFile {

	private static final long serialVersionUID = -1999171441709155756L;
	private final List<MetaChunk> metaChunks;

	public MetaFileLarge(PublicKey id, List<MetaChunk> metaChunks) {
		super(id, false);
		this.metaChunks = metaChunks;
	}

	public List<MetaChunk> getMetaChunks() {
		return metaChunks;
	}

}
