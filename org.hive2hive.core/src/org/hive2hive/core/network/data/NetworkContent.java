package org.hive2hive.core.network.data;

import java.io.Serializable;

import net.tomp2p.p2p.builder.DHTBuilder;
import net.tomp2p.peers.Number160;

/**
 * All data of <code>Hive2Hive</code> which has to be stored in the DHT are
 * using this wrapper.
 * 
 * </br> <b>Important:</b> Every wrapper class has to define a time to live
 * value.
 * 
 * @author Seppi
 */
public abstract class NetworkContent implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Some data has a domain key (used to sign content). This is the default domain key, however, by changing
	 * it, the content get signed, once it's put.
	 */
	private Number160 signature = DHTBuilder.DEFAULT_DOMAIN;

	/**
	 * All data stored in the
	 * <code>Hive2Hive<code> network has to have a timestamp in order to detect conflicts and solve race conditions.
	 * After each modification, the timestamp has to be updated as well
	 */
	private long timestamp = System.currentTimeMillis();

	/**
	 * All data stored in the <code>Hive2Hive</code> network has to have a time to live value to prevent dead
	 * content. The data wrapper with the containing data gets removed according this value.</br>
	 * <b>Important:</b> value in seconds
	 * 
	 * @return time to live
	 */
	public abstract int getTimeToLive();

	public void updateTimestamp() {
		timestamp = System.currentTimeMillis();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Number160 getSignature() {
		return signature;
	}

	protected void setSignature(Number160 signature) {
		this.signature = signature;
	}
}
