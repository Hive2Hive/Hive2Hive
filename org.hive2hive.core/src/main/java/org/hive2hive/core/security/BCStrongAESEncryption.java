package org.hive2hive.core.security;

import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Bouncy castle implementation for strong AES encryption
 * 
 * @author Nico
 *
 */
public class BCStrongAESEncryption implements IStrongAESEncryption {

	@Override
	public byte[] encryptStrongAES(byte[] data, SecretKey key, byte[] initVector) throws GeneralSecurityException {
		try {
			return processAESCipher(true, data, key, initVector);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
			throw new GeneralSecurityException("Cannot encrypt the data with AES 256bit", e);
		}
	}

	@Override
	public byte[] decryptStrongAES(byte[] data, SecretKey key, byte[] initVector) throws GeneralSecurityException {
		try {
			return processAESCipher(false, data, key, initVector);
		} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
			throw new GeneralSecurityException("Cannot decrypt the data with AES 256bit", e);
		}
	}

	private static byte[] processAESCipher(boolean encrypt, byte[] data, SecretKey key, byte[] initVector)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		// seat up engine, block cipher mode and padding
		AESEngine aesEngine = new AESEngine();
		CBCBlockCipher cbc = new CBCBlockCipher(aesEngine);
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc);

		// apply parameters
		CipherParameters parameters = new ParametersWithIV(new KeyParameter(key.getEncoded()), initVector);
		cipher.init(encrypt, parameters);

		// process ciphering
		byte[] output = new byte[cipher.getOutputSize(data.length)];

		int bytesProcessed1 = cipher.processBytes(data, 0, data.length, output, 0);
		int bytesProcessed2 = cipher.doFinal(output, bytesProcessed1);
		byte[] result = new byte[bytesProcessed1 + bytesProcessed2];
		System.arraycopy(output, 0, result, 0, result.length);
		return result;
	}
}
