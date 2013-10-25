package org.hive2hive.core.encryption;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.hive2hive.core.encryption.JavaEncryptionUtil.AES_KEYLENGTH;

/**
 * This class is used to encrypt objects for transmission. The content to transmit is encrypted in a hybrid manner.
 * @author Christian
 *
 */
public class EncryptionCapsule implements Serializable {

	private static final long serialVersionUID = 1946518588238414185L;
	
	private final JavaEncryptionUtil encryptionUtil = new JavaEncryptionUtil();
	
	private final EncryptedContent aesEncryptedContent;
	private final EncryptedContent rsaEncryptedKey;
//	private final EncryptedContent rsaEncryptedInitVector;

	public EncryptionCapsule(Object content, PublicKey publicKey) {
		
		// serialize content
		// TODO assure the object is serializable
		byte[] serializedObject = encryptionUtil.serializeObject(content);
		
		// encrypt content with AES key
		SecretKey aesKey = encryptionUtil.createAESKey(AES_KEYLENGTH.BIT_128);
		aesEncryptedContent = encryptionUtil.encryptAES(serializedObject, aesKey);

		// encrypt key and initialization vector with RSA		
		rsaEncryptedKey = encryptionUtil.encryptRSA(aesKey.getEncoded(), publicKey);
//		rsaEncryptedInitVector = EncryptionUtil.encryptRSA(aesEncryptedContent.getInitVector(), publicKey);
	}

	public Object getContent(PrivateKey privateKey) {
		
		// decrypt key and initialization vector with RSA
		byte[] rsaDecryptedKey = encryptionUtil.decryptRSA(rsaEncryptedKey, privateKey);
//		byte[] rsaDecryptedInitVector = EncryptionUtil.decryptRSA(rsaEncryptedInitVector, privateKey);
		
		// decrypt content with AES
		SecretKeySpec aesKey = new SecretKeySpec(rsaDecryptedKey, "AES");
		byte[] aesDecryptedContent = encryptionUtil.decryptAES(aesEncryptedContent, aesKey);
		
		// deserialize content
		return encryptionUtil.deserializeObject(aesDecryptedContent);
	}
}
