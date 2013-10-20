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

	private final Object content;

	public Object getContent() {
		return content;
	}

	public abstract int getTimeToLive();

	public DataWrapper(Object content) {
		this.content = content;
	}

}
