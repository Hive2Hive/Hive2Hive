package org.hive2hive.core.encryption;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;

/**
 * This class is used to encrypt objects for transmission. The content to transmit is encrypted in a hybrid manner.
 * @author Christian
 *
 */
public class EncryptionCapsule implements Serializable {

	private static final long serialVersionUID = 1946518588238414185L;
	
	private final EncryptedContent aesEncryptedContent;
	private final EncryptedContent rsaEncryptedKey;
//	private final EncryptedContent rsaEncryptedInitVector;

	public EncryptionCapsule(Object content, PublicKey publicKey) {
		
		// serialize content
		// TODO assure the object is serializable
		byte[] serializedObject = EncryptionUtil.serializeObject(content);
		
		// encrypt content with AES key
		SecretKey aesKey = EncryptionUtil.createAESKey(AES_KEYLENGTH.BIT_128);
		aesEncryptedContent = EncryptionUtil.encryptAES(serializedObject, aesKey);

		// encrypt key and initialization vector with RSA		
		rsaEncryptedKey = EncryptionUtil.encryptRSA(aesKey.getEncoded(), publicKey);
//		rsaEncryptedInitVector = EncryptionUtil.encryptRSA(aesEncryptedContent.getInitVector(), publicKey);
	}

	public Object getContent(PrivateKey privateKey) {
		
		// decrypt key and initialization vector with RSA
		byte[] rsaDecryptedKey = EncryptionUtil.decryptRSA(rsaEncryptedKey, privateKey);
//		byte[] rsaDecryptedInitVector = EncryptionUtil.decryptRSA(rsaEncryptedInitVector, privateKey);
		
		// decrypt content with AES
		SecretKeySpec aesKey = new SecretKeySpec(rsaDecryptedKey, "AES");
		byte[] aesDecryptedContent = EncryptionUtil.decryptAES(aesEncryptedContent, aesKey);
		
		// deserialize content
		return EncryptionUtil.deserializeObject(aesDecryptedContent);
	}
}
