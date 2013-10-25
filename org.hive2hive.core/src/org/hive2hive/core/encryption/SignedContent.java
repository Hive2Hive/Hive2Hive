package org.hive2hive.core.encryption;

/**
 * This class is used to keep track of signature information of a signed content, which will be used once the content needs to be verified.
 * @author Christian
 *
 */
public final class SignedContent {

	private byte[] originalData;
	private byte[] signatureBytes;
	
	public SignedContent(byte[] originalData, byte[] signatureBytes){
		this.originalData = originalData;
		this.signatureBytes = signatureBytes;
	}
	
	public byte[] getOriginalData() {
		return originalData;
	}
	
	public byte[] getSignatureBytes() {
		return signatureBytes;
	}
}
