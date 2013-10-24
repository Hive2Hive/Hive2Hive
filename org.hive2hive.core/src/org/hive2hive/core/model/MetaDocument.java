package org.hive2hive.core.model;

import java.security.PublicKey;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkData;

/**
 * Abstract class for a meta document (can either be a file or a folder). The documents are identified by the
 * public key and can be decrypted using the private key
 * 
 * @author Nico
 * 
 */
public abstract class MetaDocument extends NetworkData {

	private static final long serialVersionUID = 1L;
	private final PublicKey id;

	public MetaDocument(PublicKey id) {
		this.id = id;
	}

	public PublicKey getId() {
		return id;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getMetaDocument();
	}

}
