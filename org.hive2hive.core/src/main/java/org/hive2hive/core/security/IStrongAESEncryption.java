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
	 * @param data
	 * @param key
	 * @param initVector
	 * @return encrypted data
	 * @throws GeneralSecurityException
	 */
	byte[] encryptStrongAES(byte[] data, SecretKey key, byte[] initVector) throws GeneralSecurityException;

	/**
	 * Decrypt the data with a large AES key
	 * 
	 * @param data
	 * @param key
	 * @param initVector
	 * @return decrypted data
	 * @throws GeneralSecurityException
	 */
	byte[] decryptStrongAES(byte[] data, SecretKey key, byte[] initVector) throws GeneralSecurityException;
}
