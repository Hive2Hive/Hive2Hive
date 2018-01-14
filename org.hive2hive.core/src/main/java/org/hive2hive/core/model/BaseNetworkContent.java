package org.hive2hive.core.model;

import java.io.Serializable;

/**
 * All data of <code>Hive2Hive</code> which has to be stored in the DHT are
 * using this wrapper. <br>
 * <b>Important:</b> Every wrapper class has to define a time to live
 * value. <br>
 * 
 * @author Nico, Seppi
 */
public abstract class BaseNetworkContent implements Serializable {

	private static final long serialVersionUID = 5320162170420290193L;

	/**
	 * All data stored in the <code>Hive2Hive</code> network has to have a time to live value to prevent dead
	 * content. The data wrapper with the containing data gets removed according this value.<br>
	 * <b>Important:</b> value in seconds
	 * 
	 * @return time to live
	 */
	public abstract int getTimeToLive();
}
