package org.hive2hive.core.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;

public interface IH2HEncryption {

	/**
	 * @return the installed security provider identifier. This can for example be "BC" for BouncyCastle.
	 */
	String getSecurityProvider();

	/**
	 * Symmetrically encrypts content inheriting from {@link BaseNetworkContent} by means of the AES
	 * algorithm.
	 * The content first gets serialized, then encrypted.
	 * 
	 * @param content the content to be encrypted. Can be of any type that extends {@link BaseNetworkContent}.
	 * @param aesKey The symmetric key with which the content will be encrypted.
	 * @return EncryptedContent which contains the encrypted byte[] content as well as the AES initialization
	 *         vector (IV).
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	EncryptedNetworkContent encryptAES(BaseNetworkContent content, SecretKey aesKey) throws IOException,
			GeneralSecurityException;

	/**
	 * Symmetrically decrypts a prior content inheriting from {@link BaseNetworkContent} by means of the AES
	 * algorithm. The content gets deserialized after the decryption.
	 * 
	 * @param content The EncryptedContent to be decrypted.
	 * @param aesKey The symmetric key with which the content will be decrypted.
	 * @return decrypted object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws GeneralSecurityException
	 */
	BaseNetworkContent decryptAES(EncryptedNetworkContent content, SecretKey aesKey) throws ClassNotFoundException,
			IOException, GeneralSecurityException;

	/**
	 * Asymmetrically encrypts content inheriting from {@link BaseNetworkContent}. A default key length will
	 * be used.
	 * 
	 * @param content the content to be encrypted.
	 * @param publicKey The asymmetric public key with which the content will be encrypted
	 * @return the encrypted content
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	HybridEncryptedContent encryptHybrid(BaseNetworkContent content, PublicKey publicKey) throws IOException,
			GeneralSecurityException;

	/**
	 * Asymmetrically encrypts any content that is already serialized. A default key length will be
	 * used.
	 * 
	 * @param content the content to be encrypted.
	 * @param publicKey The asymmetric public key with which the content will be encrypted
	 * @return the encrypted content
	 * @throws GeneralSecurityException
	 */
	HybridEncryptedContent encryptHybrid(byte[] content, PublicKey publicKey) throws GeneralSecurityException;

	/**
	 * Asymmetrically decrypts a prior content inheriting from {@link BaseNetworkContent}.
	 * 
	 * @param content the encrypted content to be decrypted
	 * @param privateKey the asymmetric private key that matches the public key at encryption
	 * @return decrypted object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws GeneralSecurityException
	 */
	BaseNetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey) throws ClassNotFoundException,
			IOException, GeneralSecurityException;

	/**
	 * Asymmetrically decrypts any content
	 * 
	 * @param content the encrypted content to be decrypted
	 * @param privateKey the asymmetric private key that matches the public key at encryption
	 * @return decrypted object in the raw format
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws GeneralSecurityException
	 */
	byte[] decryptHybridRaw(HybridEncryptedContent content, PrivateKey privateKey) throws ClassNotFoundException,
			IOException, GeneralSecurityException;

}