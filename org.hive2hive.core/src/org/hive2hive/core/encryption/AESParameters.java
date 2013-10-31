package org.hive2hive.core.encryption;

import java.io.Serializable;

/**
 * This class encapsulates the parameters used for symmetric AES encryption. This includes the encoded AES
 * key and the initialization vector (IV).
 * 
 * @author Christian
 * 
 */
public final class AESParameters implements Serializable {

	private static final long serialVersionUID = -7571488981717241025L;
	
	private final byte[] encodedKey;
	private final byte[] initVector;

	public AESParameters(byte[] encodedKey, byte[] initVector) {
		this.encodedKey = encodedKey;
		this.initVector = initVector;
	}

	public byte[] getEncodedKey() {
		return encodedKey;
	}

	public byte[] getIV() {
		return initVector;
	}
}
