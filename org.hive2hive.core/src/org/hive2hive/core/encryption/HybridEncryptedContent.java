package org.hive2hive.core.encryption;

public final class HybridEncryptedContent {

	private final byte[] encryptedParameters;
	private final byte[] encryptedData;
	
	public HybridEncryptedContent(byte[] encryptedParams, byte[] encryptedData) {
		this.encryptedParameters = encryptedParams;
		this.encryptedData = encryptedData;
	}
	
	public byte[] getEncryptedParameters() {
		return encryptedParameters;
	}
	
	public byte[] getEncryptedData() {
		return encryptedData;
	}
}
