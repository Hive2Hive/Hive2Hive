package org.hive2hive.core.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

@Deprecated
public final class JavaEncryptionUtil {

	public enum AES_KEYLENGTH {
		BIT_128(128);
		// BIT192 (192);
		// BIT256 (256);

		private final int bitLength;

		AES_KEYLENGTH(int length) {
			bitLength = length;
		}

		public int value() {
			return bitLength;
		}
	}

	public enum RSA_KEYLENGTH {
		BIT_1024(1024),
		BIT_2048(2048),
		BIT_4096(4096);

		private final int bitLength;

		RSA_KEYLENGTH(int length) {
			bitLength = length;
		}

		public int value() {
			return bitLength;
		}
	}

	public JavaEncryptionUtil() {
	}

	public SecretKey createAESKeyFromPassword(UserPassword password, AES_KEYLENGTH keyLength)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password.getPassword(), password.getSalt(), 65536, keyLength.value());
		SecretKey tmpKey = kf.generateSecret(spec);
		SecretKey key = new SecretKeySpec(tmpKey.getEncoded(), "AES");
		return key;
	}

	/**
	 * Creates a random salt that can be used in combination with a key in order to prevent dictionary
	 * attacks.
	 * 
	 * @return A random 8 byte salt.
	 */
	public byte[] createSalt(int byteLength) {

		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[byteLength];
		random.nextBytes(salt);
		return salt;
	}
}
