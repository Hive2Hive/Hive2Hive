package org.hive2hive.core.security;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

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
		byte[] serialized = EncryptionUtil.serializeObject(content);
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
		return (NetworkContent) EncryptionUtil.deserializeObject(decrypted);
	}

	@Override
	public HybridEncryptedContent encryptHybrid(NetworkContent content, PublicKey publicKey) throws InvalidKeyException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] serialized = EncryptionUtil.serializeObject(content);

		HybridEncryptedContent encryptHybrid = EncryptionUtil.encryptHybrid(serialized, publicKey,
				H2HConstants.KEYLENGTH_HYBRID_AES);
		encryptHybrid.setTimeToLive(content.getTimeToLive());
		return encryptHybrid;
	}

	@Override
	public NetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidCipherTextException, ClassNotFoundException, IOException {
		byte[] decrypted = EncryptionUtil.decryptHybrid(content, privateKey);
		return (NetworkContent) EncryptionUtil.deserializeObject(decrypted);
	}

	/**
	 * Compares if the file md5 matches a given md5 hash
	 * 
	 * @param file
	 * @param expectedMD5
	 * @return
	 * @throws IOException
	 */
	public static boolean compareMD5(File file, byte[] expectedMD5) throws IOException {
		if (!file.exists() && expectedMD5 == null) {
			// both do not exist
			return true;
		} else if (file.isDirectory()) {
			// directories always match
			return true;
		}

		byte[] md5Hash = HashUtil.hash(file);
		return compareMD5(md5Hash, expectedMD5);
	}

	/**
	 * Compares if the given md5 matches another md5 hash. This method works symmetrically and is not
	 * dependent on the parameter order
	 * 
	 * @param md5 the hash to test
	 * @param expectedMD5 the expected md5 hash
	 * @return
	 */
	public static boolean compareMD5(byte[] md5, byte[] expectedMD5) {
		// both null values is ok
		if (md5 == null) {
			return expectedMD5 == null;
		} else if (expectedMD5 == null) {
			return md5 == null;
		}

		// calculate the MD5 hash and compare it
		return Arrays.equals(md5, expectedMD5);
	}

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
