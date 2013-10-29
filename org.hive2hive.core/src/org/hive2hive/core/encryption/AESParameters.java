package org.hive2hive.core.encryption;

/**
 * This class encapsulates the parameters used for symmetric AES encryption. This includes the secret AES
 * password and the initialization vector (IV).
 * 
 * @author Christian
 * 
 */
public final class AESParameters {

	private final byte[] aesKey;
	private final byte[] initVector;

	public AESParameters(byte[] aesKey, byte[] initVector) {
		this.aesKey = aesKey;
		this.initVector = initVector;
	}

	public byte[] getAESKey() {
		return aesKey;
	}

	public byte[] getIV() {
		return initVector;
	}
}
