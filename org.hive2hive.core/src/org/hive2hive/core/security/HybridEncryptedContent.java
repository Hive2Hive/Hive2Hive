package org.hive2hive.core.security;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * This class contains the result of a hybrid encryption. It holds the RSA encrypted parameters and the AES
 * encrypted data.
 * 
 * @author Christian, Nico
 * 
 */
public final class HybridEncryptedContent extends NetworkContent {

	private static final long serialVersionUID = 1L;
	private final byte[] encryptedParameters;
	private final byte[] encryptedData;
	private int timeToLive = TimeToLiveStore.convertDaysToSeconds(365);

	public HybridEncryptedContent(byte[] encryptedParams, byte[] encryptedData) {
		this.encryptedParameters = encryptedParams;
		this.encryptedData = encryptedData;
	}

	/**
	 * Get the RSA encrypted parameters.
	 * 
	 * @return
	 */
	public byte[] getEncryptedParameters() {
		return encryptedParameters;
	}

	/**
	 * Get the AES encrypted data.
	 * 
	 * @return
	 */
	public byte[] getEncryptedData() {
		return encryptedData;
	}

	@Override
	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
}
