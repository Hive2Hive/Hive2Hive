package org.hive2hive.core.network.data;

import java.io.Serializable;

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
	 * Some data has a version key (used to differentiate versions).
	 */
	private Number160 versionKey = Number160.ZERO;

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

	public Number160 getVersionKey() {
		return versionKey;
	}

	protected void setVersionKey(Number160 versionKey) {
		this.versionKey = versionKey;
	}
}
