package org.hive2hive.core.encryption;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkData;

/**
 * This class holds the bytes of an encrypted content and its initialization vector.
 * 
 * @author Christian
 * 
 */
public final class EncryptedContent extends NetworkData {

	private static final long serialVersionUID = 1L;
	private final byte[] cipherContent;
	private final byte[] initVector;
	private int timeToLive = TimeToLiveStore.convertDaysToSeconds(365);

	public EncryptedContent(byte[] cipherContent, byte[] initVector) {
		this.cipherContent = cipherContent;
		this.initVector = initVector;
	}

	public final byte[] getCipherContent() {
		return cipherContent;
	}

	public final byte[] getInitVector() {
		return initVector;
	}

	@Override
	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
}
