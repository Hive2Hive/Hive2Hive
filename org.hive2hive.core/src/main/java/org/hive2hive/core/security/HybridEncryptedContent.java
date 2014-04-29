package org.hive2hive.core.security;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.model.NetworkContent;

/**
 * This class contains the result of a hybrid encryption. It holds the RSA encrypted parameters and the AES
 * encrypted data.
 * 
 * @author Christian, Nico
 * 
 */
public final class HybridEncryptedContent extends NetworkContent {

	private static final long serialVersionUID = -1612926603789157681L;

	private final byte[] encryptedParameters;
	private final byte[] encryptedData;
	private int timeToLive = TimeToLiveStore.convertDaysToSeconds(365);

	private String userId = null;
	private byte[] signature = null;

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

	/**
	 * Set signature.
	 * 
	 * @param userId
	 *            the creator of the signature
	 * @param signature
	 *            the signature
	 */
	public void setSignature(String userId, byte[] signature) {
		this.userId = userId;
		this.signature = signature;
	}

	/**
	 * Getter
	 * 
	 * @return the creator of the signature
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Getter
	 * 
	 * @return the signature of this message
	 */
	public byte[] getSignature() {
		return signature;
	}

	@Override
	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
}
