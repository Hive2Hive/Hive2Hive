package org.hive2hive.core.encryption;

/**
 * This class holds the bytes of an encrypted content and its initialization vector.
 * @author Christian
 *
 */
public final class EncryptedContent {

	private final byte[] cipherContent;
	private final byte[] initVector;
	
	public EncryptedContent(byte[] cipherContent, byte[] initVector){
		this.cipherContent = cipherContent;
		this.initVector = initVector;
	}
	
	public final byte[] getCipherContent(){
		return cipherContent;
	}
	
	public final byte[] getInitVector(){
		return initVector;
	}
}
