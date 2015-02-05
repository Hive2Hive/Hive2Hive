package org.hive2hive.core.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;

public class H2HDefaultEncryption implements IH2HEncryption {

	private final IH2HSerialize serializer;
	private final String securityProvider;
	private final IStrongAESEncryption strongAES;

	/**
	 * Create a default encryption using bouncy castle as the security provider
	 */
	public H2HDefaultEncryption(IH2HSerialize serializer) {
		this(serializer, BouncyCastleProvider.PROVIDER_NAME, new BCStrongAESEncryption());

		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	/**
	 * Create a default encryption using any installed security provider identifier.
	 * 
	 * @param serializer the serializer to encode / decode objects
	 * @param securityProvider the security provider identifier. Note that the provider must be installed
	 *            separately.
	 * @param strongAES the fallback if the AES encryption / decryption has a too long key
	 */
	public H2HDefaultEncryption(IH2HSerialize serializer, String securityProvider, IStrongAESEncryption strongAES) {
		this.serializer = serializer;
		this.securityProvider = securityProvider;
		this.strongAES = strongAES;
	}

	@Override
	public String getSecurityProvider() {
		return securityProvider;
	}

	@Override
	public EncryptedNetworkContent encryptAES(BaseNetworkContent content, SecretKey aesKey) throws IOException,
			GeneralSecurityException {
		byte[] serialized = serializer.serialize(content);
		byte[] initVector = EncryptionUtil.generateIV();
		byte[] encryptedContent = EncryptionUtil.encryptAES(serialized, aesKey, initVector, securityProvider, strongAES);

		EncryptedNetworkContent encryptedNetworkContent = new EncryptedNetworkContent(encryptedContent, initVector);
		encryptedNetworkContent.setTimeToLive(content.getTimeToLive());
		return encryptedNetworkContent;
	}

	@Override
	public BaseNetworkContent decryptAES(EncryptedNetworkContent content, SecretKey aesKey) throws ClassNotFoundException,
			IOException, GeneralSecurityException {
		byte[] decrypted = EncryptionUtil.decryptAES(content.getCipherContent(), aesKey, content.getInitVector(),
				securityProvider, strongAES);
		return (BaseNetworkContent) serializer.deserialize(decrypted);
	}

	@Override
	public HybridEncryptedContent encryptHybrid(BaseNetworkContent content, PublicKey publicKey) throws IOException,
			GeneralSecurityException {
		byte[] serialized = serializer.serialize(content);

		HybridEncryptedContent encryptHybrid = encryptHybrid(serialized, publicKey);
		encryptHybrid.setTimeToLive(content.getTimeToLive());
		return encryptHybrid;
	}

	@Override
	public HybridEncryptedContent encryptHybrid(byte[] content, PublicKey publicKey) throws GeneralSecurityException {
		return EncryptionUtil.encryptHybrid(content, publicKey, H2HConstants.KEYLENGTH_HYBRID_AES, securityProvider,
				strongAES);
	}

	@Override
	public BaseNetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey)
			throws ClassNotFoundException, IOException, GeneralSecurityException {
		return (BaseNetworkContent) serializer.deserialize(decryptHybridRaw(content, privateKey));
	}

	@Override
	public byte[] decryptHybridRaw(HybridEncryptedContent content, PrivateKey privateKey) throws ClassNotFoundException,
			IOException, GeneralSecurityException {
		return EncryptionUtil.decryptHybrid(content, privateKey, securityProvider, strongAES);
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
	 * Compares two key pairs (either one can be null)
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
