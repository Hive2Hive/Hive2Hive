package org.hive2hive.core.encryption;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.hive2hive.core.encryption.JavaEncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.JavaEncryptionUtil.RSA_KEYLENGTH;

public interface IEncryptionUtil {

	public byte[] serializeObject(Object object);

	public Object deserializeObject(byte[] bytes);

	public EncryptedContent encryptAES(byte[] content, SecretKey aesKey);

	public byte[] decryptAES(EncryptedContent content, SecretKey aesKey);

	public EncryptedContent encryptRSA(byte[] content, PublicKey publicKey);

	public byte[] decryptRSA(EncryptedContent content, PrivateKey privateKey);

	public SecretKey createDESKey(String password, byte[] salt);

	public SecretKey createAESKey(AES_KEYLENGTH keyLength);

	public KeyPair createRSAKeys(RSA_KEYLENGTH keyLength);

	public SecretKey createAESKeyFromPassword(UserPassword password, AES_KEYLENGTH keyLength);

	public byte[] createSalt(int byteLength);

	public SignedContent sign(byte[] content, PrivateKey privateKey);

	public boolean verify(SignedContent content, PublicKey publicKey);

	public byte[] toByte(String string);

	public String toString(byte[] bytes);
}
