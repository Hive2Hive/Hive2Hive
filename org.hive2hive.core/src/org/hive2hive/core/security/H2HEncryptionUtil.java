package org.hive2hive.core.security;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;

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

	/**
	 * Asymmetrically encrypts content inheriting from {@link NetworkContent}.
	 * 
	 * @param content the content to be encrypted.
	 * @param publicKey The asymmetric public key with which the content will be encrypted
	 * @param keyLength the strength of the encryption
	 * @return the encrypted content
	 * @throws DataLengthException
	 * @throws InvalidKeyException
	 * @throws IllegalStateException
	 * @throws InvalidCipherTextException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static HybridEncryptedContent encryptHybrid(NetworkContent content, PublicKey publicKey,
			AES_KEYLENGTH keyLength) throws DataLengthException, InvalidKeyException, IllegalStateException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException {
		byte[] serialized = EncryptionUtil.serializeObject(content);

		HybridEncryptedContent encryptHybrid = EncryptionUtil.encryptHybrid(serialized, publicKey, keyLength);
		encryptHybrid.setTimeToLive(content.getTimeToLive());
		return encryptHybrid;
	}

	/**
	 * Asymmetrically decrypts a prior content inheriting from {@link NetworkContent}.
	 * 
	 * @param content the encrypted content to be decrypted
	 * @param privateKey the asymmetric private key that matches the public key at encryption
	 * @return decrypted object
	 * @throws InvalidKeyException
	 * @throws DataLengthException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IllegalStateException
	 * @throws InvalidCipherTextException
	 */
	public static NetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey)
			throws InvalidKeyException, DataLengthException, IllegalBlockSizeException, BadPaddingException,
			IllegalStateException, InvalidCipherTextException {
		byte[] decrypted = EncryptionUtil.decryptHybrid(content, privateKey);
		return (NetworkContent) EncryptionUtil.deserializeObject(decrypted);
	}

	/**
	 * Compares if the data matches a given md5 hash
	 * 
	 * @param data the data to generate the md5 hash over it
	 * @param expectedMD5 the expected md5 hash
	 * @return
	 */
	public static boolean compareMD5(byte[] data, byte[] expectedMD5) {
		// calculate the MD5 hash and compare it
		byte[] dataMD5hash = EncryptionUtil.generateMD5Hash(data);
		return new String(dataMD5hash, H2HConstants.ENCODING_CHARSET).equalsIgnoreCase(new String(
				expectedMD5, H2HConstants.ENCODING_CHARSET));
	}
}
