package org.hive2hive.core.model.versioned;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.model.BaseNetworkContent;

/**
 * All data of <code>Hive2Hive</code> which has to be stored in the DHT and are frequently manipulated are
 * using this wrapper.
 * 
 * @author Seppi
 */
public abstract class BaseVersionedNetworkContent extends BaseNetworkContent {

	private static final long serialVersionUID = 8206000167141687813L;

	/**
	 * Some data has a version key (used to differentiate versions). Default value.
	 */
	private Number160 versionKey = Number160.ZERO;

	/**
	 * Some data is based on other data. Default value.
	 */
	private Number160 basedOnKey = Number160.ZERO;

	public Number160 getVersionKey() {
		return versionKey;
	}

	public void setVersionKey(Number160 versionKey) {
		this.versionKey = versionKey;
	}

	public Number160 getBasedOnKey() {
		return basedOnKey;
	}

	public void setBasedOnKey(Number160 basedOnKey) {
		this.basedOnKey = basedOnKey;
	}

	/**
	 * Call this method in front of a put into the network if the data is a new version or will have other
	 * versions.
	 */
	public void generateVersionKey() {
		// re-attach version keys
		basedOnKey = versionKey;
		// increase counter
		long counter = basedOnKey.timestamp() + 1;
		// create new version key based on increased counter and hash
		versionKey = new Number160(counter, new Number160(this.hashCode()).number96());
	}
}
