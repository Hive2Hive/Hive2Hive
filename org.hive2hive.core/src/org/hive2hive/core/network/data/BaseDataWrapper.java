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
public abstract class BaseDataWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Object content;

	public Object getContent() {
		return content;
	}

	/**
	 * All data stored in the <code>Hive2Hive</code> network has to have a time to live value to prevent dead
	 * content. The data wrapper with the containing data gets removed according this value.</br>
	 * <b>Important:</b> value in seconds
	 * 
	 * @return time to live 
	 */
	public abstract int getTimeToLive();

	public BaseDataWrapper(Object content) {
		this.content = content;
	}

}
