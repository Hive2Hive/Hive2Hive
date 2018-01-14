package org.hive2hive.core.security;

import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

/**
 * There is a policy that java is not allowed to encrypt / decrypt keys larger than 128 bytes.
 * Therefore, this class acts as a fallback if the key is "too long".
 * 
 * @author Nico
 *
 */
public interface IStrongAESEncryption {

	/**
	 * Encrypt the data with a large AES key
	 * 
	 * @param data the data
	 * @param key the secret key
	 * @param initVector the init-vector
	 * @return encrypted the encrypted data
	 * @throws GeneralSecurityException if the data cannot be encrypted for any reason.
	 */
	byte[] encryptStrongAES(byte[] data, SecretKey key, byte[] initVector) throws GeneralSecurityException;

	/**
	 * Decrypt the data with a large AES key
	 * 
	 * @param data the data
	 * @param key the secret key
	 * @param initVector the init-vector
	 * @return decrypted datathe decrypted data
	 * @throws GeneralSecurityException if the data cannot be decrypted for any reason.
	 */
	byte[] decryptStrongAES(byte[] data, SecretKey key, byte[] initVector) throws GeneralSecurityException;
}
