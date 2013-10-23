package org.hive2hive.core.network.data;

import java.io.Serializable;

/**
 * All data of <code>Hive2Hive</code> which has to be stored in the DHT are
 * using this wrapper.
 * 
 * </br> <b>Important:</b> Every wrapper class has to define a time to live
 * value.
 * 
 * @author Seppi
 */
public abstract class DataWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * All data stored in the
	 * <code>Hive2Hive<code> network has to have a version in order to detect conflicts and race conditions.
	 * After each modification, the version has to increase by 1
	 */
	private int version = 0;

	/**
	 * All data stored in the <code>Hive2Hive</code> network has to have a time to live value to prevent dead
	 * content. The data wrapper with the containing data gets removed according this value.</br>
	 * <b>Important:</b> value in seconds
	 * 
	 * @return time to live
	 */
	public abstract int getTimeToLive();

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Increases the version by 1
	 */
	public void increaseVersion() {
		version = (version + 1) % Integer.MAX_VALUE;
	}

}
