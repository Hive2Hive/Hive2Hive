package org.hive2hive.core.model;

import java.security.PublicKey;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * Abstract class for a meta document (can either be a file or a folder). The documents are identified by the
 * public key and can be decrypted using the private key
 * 
 * @author Nico
 * 
 */
public abstract class MetaDocument extends NetworkContent {

	private static final long serialVersionUID = 1L;
	private final PublicKey id;
	private final String name;

	public MetaDocument(PublicKey id, String name) {
		this.id = id;
		this.name = name;
	}

	public PublicKey getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getMetaDocument();
	}

}
