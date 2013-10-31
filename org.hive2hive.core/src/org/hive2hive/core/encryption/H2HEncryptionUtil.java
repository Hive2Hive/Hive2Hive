package org.hive2hive.core.encryption;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.network.data.NetworkContent;

public final class H2HEncryptionUtil {

	private H2HEncryptionUtil() {
	}

	/**
	 * Symmetrically encrypts content inheriting from {@link NetworkContent} by means of the AES algorithm.
	 * The content first gets serialized, then encrypted.
	 * 
	 * @param content the content to be encrypted. Can be of any type that extends {@link NetworkContent}.
	 * @param aesKey The symmetric key with which the content will be encrypted.
	 * @return EncryptedContent which contains the encrypted byte[] content as well as the AES initialization
	 *         vector (IV).
	 * @throws InvalidCipherTextException
	 * @throws IllegalStateException
	 * @throws DataLengthException
	 */
	public static EncryptedNetworkContent encryptAES(NetworkContent content, SecretKey aesKey)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		byte[] serialized = EncryptionUtil.serializeObject(content);
		byte[] initVector = EncryptionUtil.generateIV();
		byte[] encryptedContent = EncryptionUtil.encryptAES(serialized, aesKey, initVector);

		EncryptedNetworkContent encryptedNetworkContent = new EncryptedNetworkContent(encryptedContent,
				initVector);
		encryptedNetworkContent.setTimeToLive(content.getTimeToLive());
		return encryptedNetworkContent;
	}

	/**
	 * Symmetrically decrypts a prior content inheriting from {@link NetworkContent} by means of the AES
	 * algorithm. The content gets deserialized after the decryption.
	 * 
	 * @param content The EncryptedContent to be decrypted.
	 * @param aesKey The symmetric key with which the content will be decrypted.
	 * @return decrypted object
	 * @throws InvalidCipherTextException
	 * @throws IllegalStateException
	 * @throws DataLengthException
	 */
	public static NetworkContent decryptAES(EncryptedNetworkContent content, SecretKey aesKey)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		byte[] decrypted = EncryptionUtil.decryptAES(content.getCipherContent(), aesKey,
				content.getInitVector());
		return (NetworkContent) EncryptionUtil.deserializeObject(decrypted);
	}
}
