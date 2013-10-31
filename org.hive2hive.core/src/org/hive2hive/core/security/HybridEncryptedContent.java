package org.hive2hive.core.security;

/**
 * This class contains the result of a hybrid encryption. It holds the RSA encrypted parameters and the AES encrypted data.
 * @author Christian
 *
 */
public final class HybridEncryptedContent {

	private final byte[] encryptedParameters;
	private final byte[] encryptedData;
	
	public HybridEncryptedContent(byte[] encryptedParams, byte[] encryptedData) {
		this.encryptedParameters = encryptedParams;
		this.encryptedData = encryptedData;
	}
	
	/**
	 * Get the RSA encrypted parameters.
	 * @return
	 */
	public byte[] getEncryptedParameters() {
		return encryptedParameters;
	}
	
	/**
	 * Get the AES encrypted data.
	 * @return
	 */
	public byte[] getEncryptedData() {
		return encryptedData;
	}
}
