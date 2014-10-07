package org.hive2hive.core.model;

import java.io.IOException;
import java.io.Serializable;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.network.NetworkManager;

/**
 * All data of <code>Hive2Hive</code> which has to be stored in the DHT are
 * using this wrapper. </br>
 * <b>Important:</b> Every wrapper class has to define a time to live
 * value. </br>
 * <b>Important:</b> If this object will be put into the network (see
 * {@link NetworkManager#putGlobal(String, String, NetworkContent)} or
 * {@link NetworkManager#putLocal(String, String, NetworkContent)}) and there exist already other
 * versions of this object you have to set {@link NetworkContent#basedOnKey} with the version key of the
 * previous version. If this will be an initial put you don't have to (default value {@link Number160#ZERO}).
 * In both cases you have to call {@link NetworkContent#generateVersionKey()} to generate a fresh version key
 * which contains a actual time stamp and hash value of the object.
 * 
 * @author Nico, Seppi
 */
public abstract class NetworkContent implements Serializable {

	private static final long serialVersionUID = 5320162170420290193L;

	/**
	 * Some data has a version key (used to differentiate versions). Default value.
	 */
	private Number160 versionKey = Number160.ZERO;

	/**
	 * Some data is based on other data. Default value.
	 */
	private Number160 basedOnKey = Number160.ZERO;

	/**
	 * All data stored in the <code>Hive2Hive</code> network has to have a time to live value to prevent dead
	 * content. The data wrapper with the containing data gets removed according this value.</br>
	 * <b>Important:</b> value in seconds
	 * 
	 * @return time to live
	 */
	public abstract int getTimeToLive();

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
	 * 
	 * @throws IOException
	 */
	public void generateVersionKey() throws IOException {
		// re-attach version keys
		basedOnKey = versionKey;
		// increase counter
		long counter = basedOnKey.timestamp() + 1;
		// create new version key based on increased counter and hash
		versionKey = new Number160(counter, new Number160(this.hashCode()).number96());
	}
}
