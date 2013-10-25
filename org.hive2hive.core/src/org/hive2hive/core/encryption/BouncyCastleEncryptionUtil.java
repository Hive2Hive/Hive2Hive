package org.hive2hive.core.encryption;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.hive2hive.core.encryption.JavaEncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.JavaEncryptionUtil.RSA_KEYLENGTH;

public final class BouncyCastleEncryptionUtil implements IEncryptionUtil {

	@Override
	public byte[] serializeObject(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object deserializeObject(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EncryptedContent encryptAES(byte[] content, SecretKey aesKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] decryptAES(EncryptedContent content, SecretKey aesKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EncryptedContent encryptRSA(byte[] content, PublicKey publicKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] decryptRSA(EncryptedContent content, PrivateKey privateKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SecretKey createDESKey(String password, byte[] salt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SecretKey createAESKey(AES_KEYLENGTH keyLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KeyPair createRSAKeys(RSA_KEYLENGTH keyLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SecretKey createAESKeyFromPassword(UserPassword password, AES_KEYLENGTH keyLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] createSalt(int byteLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SignedContent sign(byte[] content, PrivateKey privateKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verify(SignedContent content, PublicKey publicKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] toByte(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}
}
