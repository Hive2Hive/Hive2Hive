package org.hive2hive.core.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.NetworkContent;

public final class H2HDefaultEncryption implements IH2HEncryption {

	@Override
	public EncryptedNetworkContent encryptAES(NetworkContent content, SecretKey aesKey) throws InvalidCipherTextException,
			IOException {
		byte[] serialized = SerializationUtil.serialize(content);
		byte[] initVector = EncryptionUtil.generateIV();
		byte[] encryptedContent = EncryptionUtil.encryptAES(serialized, aesKey, initVector);

		EncryptedNetworkContent encryptedNetworkContent = new EncryptedNetworkContent(encryptedContent, initVector);
		encryptedNetworkContent.setTimeToLive(content.getTimeToLive());
		return encryptedNetworkContent;
	}

	@Override
	public NetworkContent decryptAES(EncryptedNetworkContent content, SecretKey aesKey) throws InvalidCipherTextException,
			ClassNotFoundException, IOException {
		byte[] decrypted = EncryptionUtil.decryptAES(content.getCipherContent(), aesKey, content.getInitVector());
		return (NetworkContent) SerializationUtil.deserialize(decrypted);
	}

	@Override
	public HybridEncryptedContent encryptHybrid(NetworkContent content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] serialized = SerializationUtil.serialize(content);

		HybridEncryptedContent encryptHybrid = encryptHybrid(serialized, publicKey);
		encryptHybrid.setTimeToLive(content.getTimeToLive());
		return encryptHybrid;
	}

	@Override
	public HybridEncryptedContent encryptHybrid(byte[] content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException {
		return EncryptionUtil.encryptHybrid(content, publicKey, H2HConstants.KEYLENGTH_HYBRID_AES);
	}

	@Override
	public NetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException, ClassNotFoundException, IOException {
		return (NetworkContent) SerializationUtil.deserialize(decryptHybridRaw(content, privateKey));
	}

	@Override
	public byte[] decryptHybridRaw(HybridEncryptedContent content, PrivateKey privateKey) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException, ClassNotFoundException, IOException {
		return EncryptionUtil.decryptHybrid(content, privateKey);
	}

	/**
	 * The toString() method of a public key
	 * 
	 * @param key
	 * @return
	 */
	public static String key2String(PublicKey key) {
		return EncryptionUtil.byteToHex(key.getEncoded());
	}

	/**
	 * Compares two keypairs (either one can be null)
	 * 
	 * @param keypair1
	 * @param keypair2
	 * @return if keypair1 matches keypair2
	 */
	public static boolean compare(KeyPair keypair1, KeyPair keypair2) {
		if (keypair1 == null) {
			return keypair2 == null;
		} else if (keypair2 == null) {
			return keypair1 == null;
		}

		return keypair1.getPrivate().equals(keypair2.getPrivate()) && keypair1.getPublic().equals(keypair2.getPublic());
	}
}
