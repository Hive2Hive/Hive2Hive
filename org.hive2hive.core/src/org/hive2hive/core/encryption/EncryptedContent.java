package org.hive2hive.core.encryption;

/**
 * This class holds the bytes of an encrypted content and its initialization vector.
 * @author Christian
 *
 */
public final class EncryptedContent {

	private final byte[] content;
	private final byte[] initVector;
	
	public EncryptedContent(byte[] content, byte[] initVector){
		this.content = content;
		this.initVector = initVector;
	}
	
	public final byte[] getContent(){
		return content;
	}
	
	public final byte[] getInitVector(){
		return initVector;
	}
}
