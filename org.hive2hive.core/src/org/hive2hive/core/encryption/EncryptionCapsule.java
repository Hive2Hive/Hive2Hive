package org.hive2hive.core.encryption;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionCapsule implements Serializable {

	private static final long serialVersionUID = 1946518588238414185L;
	private final EncryptedContent aesEncryptedContent;
	private final String rsaEncryptedKey;
	private final String rsaEncryptedInitVector;

	public EncryptionCapsule(Serializable content, PublicKey publicKey) {
		
		// encrypt content with AES key
		SecretKey aesKey = EncryptionUtil.createSecretAESKey();
		String serializedObject = EncryptionUtil.serializeObject(content);
		aesEncryptedContent = EncryptionUtil.encryptAES(serializedObject, aesKey);

		// encrypt key and initialization vector with RSA
		this.rsaEncryptedKey = EncryptionUtil.encryptRSA(EncryptionUtil.toString(aesKey.getEncoded()), publicKey);
		this.rsaEncryptedInitVector = EncryptionUtil.encryptRSA(aesEncryptedContent.getInitVector(), publicKey);
	}

	public Object getContent(PrivateKey privateKey) {
		
		// decrypt key and initialization vector with RSA
		String rsaDecryptedKey = EncryptionUtil.decryptRSA(rsaEncryptedKey, privateKey);
		String rsaDecryptedInitVector = EncryptionUtil.decryptRSA(rsaEncryptedInitVector, privateKey);
		
		// decrypt content with AES
		SecretKeySpec keySpec = new SecretKeySpec(EncryptionUtil.toByte(rsaDecryptedKey), "AES");
		String serializedObject = EncryptionUtil.decryptAES(aesEncryptedContent.getContent(), rsaDecryptedInitVector, keySpec);
		return EncryptionUtil.deserializeObject(serializedObject);
	}
}
