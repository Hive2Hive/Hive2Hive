package org.hive2hive.core.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;

public final class H2HDefaultEncryption implements IH2HEncryption {

	private final IH2HSerialize serializer;
	private final String securityProvider;

	/**
	 * Create a default encryption using bouncy castle as the security provider
	 */
	public H2HDefaultEncryption(IH2HSerialize serializer) {
		this(serializer, "BC");

		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	/**
	 * Create a default encryption using any installed security provider identifier.
	 * 
	 * @param serializer the serializer to encode / decode objects
	 * @param securityProvider the security provider identifier. Note that the provider must be installed
	 *            separately.
	 */
	public H2HDefaultEncryption(IH2HSerialize serializer, String securityProvider) {
		this.serializer = serializer;
		this.securityProvider = securityProvider;
	}

	@Override
	public String getSecurityProvider() {
		return securityProvider;
	}

	@Override
	public EncryptedNetworkContent encryptAES(BaseNetworkContent content, SecretKey aesKey)
			throws InvalidCipherTextException, IOException {
		byte[] serialized = serializer.serialize(content);
		byte[] initVector = EncryptionUtil.generateIV();
		byte[] encryptedContent = EncryptionUtil.encryptAES(serialized, aesKey, initVector);

		EncryptedNetworkContent encryptedNetworkContent = new EncryptedNetworkContent(encryptedContent, initVector);
		encryptedNetworkContent.setTimeToLive(content.getTimeToLive());
		return encryptedNetworkContent;
	}

	@Override
	public BaseNetworkContent decryptAES(EncryptedNetworkContent content, SecretKey aesKey)
			throws InvalidCipherTextException, ClassNotFoundException, IOException {
		byte[] decrypted = EncryptionUtil.decryptAES(content.getCipherContent(), aesKey, content.getInitVector());
		return (BaseNetworkContent) serializer.deserialize(decrypted);
	}

	@Override
	public HybridEncryptedContent encryptHybrid(BaseNetworkContent content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] serialized = serializer.serialize(content);

		HybridEncryptedContent encryptHybrid = encryptHybrid(serialized, publicKey);
		encryptHybrid.setTimeToLive(content.getTimeToLive());
		return encryptHybrid;
	}

	@Override
	public HybridEncryptedContent encryptHybrid(byte[] content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException {
		return EncryptionUtil.encryptHybrid(content, publicKey, H2HConstants.KEYLENGTH_HYBRID_AES, securityProvider);
	}

	@Override
	public BaseNetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException,
			ClassNotFoundException, IOException {
		return (BaseNetworkContent) serializer.deserialize(decryptHybridRaw(content, privateKey));
	}

	@Override
	public byte[] decryptHybridRaw(HybridEncryptedContent content, PrivateKey privateKey) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException, ClassNotFoundException, IOException {
		return EncryptionUtil.decryptHybrid(content, privateKey, securityProvider);
	}

	/**
	 * The toString() method of a public key
	 * 
	 * @param key
	 * @return the string representation of the public key
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
