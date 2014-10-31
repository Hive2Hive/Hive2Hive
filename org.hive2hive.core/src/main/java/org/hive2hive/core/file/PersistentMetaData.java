package org.hive2hive.core.file;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hive2hive.core.network.data.download.BaseDownloadTask;

/**
 * Holds all meta objects that need to be stored when a client goes offline. These data is used when a client
 * comes online again to compare the changes during absence.
 * 
 * @author Nico
 * 
 */
public class PersistentMetaData implements Serializable {

	private static final long serialVersionUID = -1069468683019402537L;

	private Map<String, PublicKey> publicKeyCache;
	private Set<BaseDownloadTask> downloads;

	public PersistentMetaData() {
		publicKeyCache = new HashMap<String, PublicKey>(0);
		downloads = new HashSet<BaseDownloadTask>(0);
	}

	public Map<String, PublicKey> getPublicKeyCache() {
		return publicKeyCache;
	}

	public void setPublicKeyCache(Map<String, PublicKey> publicKeyCache) {
		this.publicKeyCache = publicKeyCache;
	}

	public Set<BaseDownloadTask> getDownloads() {
		return downloads;
	}

	public void setDownloads(Set<BaseDownloadTask> downloads) {
		this.downloads = downloads;
	}
}