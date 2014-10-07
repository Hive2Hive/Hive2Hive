package org.hive2hive.core.model.versioned;

import java.security.PublicKey;

import org.hive2hive.core.TimeToLiveStore;

/**
 * Base class for holding meta data for either a large or a small file
 * 
 * @author Nico
 * 
 */
public abstract class BaseMetaFile extends BaseVersionedNetworkContent {

	private static final long serialVersionUID = -7819224117319481636L;

	protected final PublicKey id;
	private final boolean isSmall;

	public BaseMetaFile(PublicKey id, boolean isSmall) {
		this.id = id;
		this.isSmall = isSmall;
	}

	public PublicKey getId() {
		return id;
	}

	public boolean isSmall() {
		return isSmall;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getMetaFile();
	}
}
