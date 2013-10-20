package org.hive2hive.core.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.eclipse.emf.ecore.xml.type.internal.DataValue.Base64;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class EncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(EncryptionUtil.class);

	public static final String ISO_8859_1 = "ISO-8859-1";
	public static final String UTF_8 = "UTF-8";
	public static final String DELIMITER = "DELIMITERDELIMITERDELIMITERDELIMITER";

	private static final String AES_CIPHER_MODE = "AES/CBC/PKCS5PADDING";
	private static final String RSA_CIPHER_MODE = "RSA";

	private EncryptionUtil() {
	}

	private static void encrypt(byte[] content, Key key, int opmode){
		
	}
	
	public static EncryptedContent encryptAES(byte[] content, SecretKey aesKey) {

		//encrypt(content, aesKey);
		byte[] encryptedContent = null;
		byte[] initVector = null;

		try {
			Cipher cipher = Cipher.getInstance(AES_CIPHER_MODE);
			try {
				// initialize cipher with mode and key
				cipher.init(Cipher.ENCRYPT_MODE, aesKey);
				try {
					// encrypt the content
					encryptedContent = cipher.doFinal(content);
					initVector = cipher.getIV();

				} catch (IllegalBlockSizeException | BadPaddingException e) {
					logger.error("Exception during encryption:", e);
				}
			} catch (InvalidKeyException e) {
				logger.error("Invalid key:", e);
			}

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Error during cipher initialisation:", e);
		}
		return new EncryptedContent(encryptedContent, initVector);
	}

	public static byte[] decryptAES(byte[] content, String initVector, SecretKey aesKey) {

		byte[] decryptedContent = null;

		try {
			Cipher cipher = Cipher.getInstance(AES_CIPHER_MODE);
			try {
				// initialize cipher with mode and key
				cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(toByte(initVector)));
				try {
					// decrypt the content
					decryptedContent = cipher.doFinal(content);

				} catch (IllegalBlockSizeException | BadPaddingException e) {
					logger.error("Exception during decryption:", e);
				}
			} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
				logger.error("Exception during initialisation of the cipher:", e);
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Error during cipher initialisation:", e);
		}
		return decryptedContent;
	}

	public static EncryptedContent encryptRSA(byte[] content, PublicKey publicKey) {
		//encrypt(content, publicKey);
		byte[] encryptedContent = null;
		byte[] initVector = null;

		try {
			Cipher cipher = Cipher.getInstance(RSA_CIPHER_MODE);
			try {
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				try {
					// encrypt the content
					encryptedContent = cipher.doFinal(content);
					initVector = cipher.getIV();
				} catch (IllegalBlockSizeException | BadPaddingException e) {
					logger.error("Exception during encryption:", e);
				}
			} catch (InvalidKeyException e) {
				logger.error("Invalid key:", e);
			}

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Error during cipher initialisation:", e);
		}
		return new EncryptedContent(encryptedContent, initVector);
	}

	public static byte[] decryptRSA(byte[] content, PrivateKey privateKey) {
		// try {
		// Cipher c = Cipher.getInstance("RSA");
		// c.init(Cipher.DECRYPT_MODE, aPrivateKey);
		// return toString(c.doFinal(toByte(aStringToDecrypt)));
		// } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
		// | IllegalBlockSizeException | BadPaddingException e) {
		// logger.error("Exception during decryption:", e);
		// }
		// return null;
		// }

		byte[] decryptedContent = null;

		try {
			Cipher cipher = Cipher.getInstance(RSA_CIPHER_MODE);
			try {
				// initialize cipher with mode and key
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
				try {
					// decrypt the content
					decryptedContent = cipher.doFinal(content);

				} catch (IllegalBlockSizeException | BadPaddingException e) {
					logger.error("Exception during decryption:", e);
				}
			} catch (InvalidKeyException e) {
				logger.error("Exception during initialisation of the cipher:", e);
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Error during cipher initialisation:", e);
		}
		return decryptedContent;
	}

	public static byte[] createRandomAESKey() {
		SecureRandom random = new SecureRandom();
		byte[] aesKey = new byte[16]; // 16 bytes = 128 bits
		random.nextBytes(aesKey);
		return aesKey;
	}

	public static SecretKey createSecretAESKey() {
		KeyGenerator keyGenerator;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			// For more security use http://www.bouncycastle.org/
			keyGenerator.init(128);
			SecretKey key = keyGenerator.generateKey();
			return key;
		} catch (NoSuchAlgorithmException e) {
			logger.error("Error during key generation:", e);
		}
		return null;
	}

	/**
	 * Convenience method to convert a String to a byte array using the
	 * ISO-8859-1 char set for conversion.
	 * 
	 * @param the
	 *            String to convert
	 * @return the conversion result or <code>null</code> if the conversion
	 *         fails.
	 */
	public static byte[] toByte(String aString) {
		try {
			byte[] result = aString.getBytes(ISO_8859_1);
			return result;
		} catch (UnsupportedEncodingException e) {
			logger.error("Can't convert String to byte[]:", e);
		}
		return null;
	}

	/**
	 * Convenience method to convert a byte array to a String using the
	 * ISO-8859-1 char set for conversion.
	 * 
	 * @param someBytes
	 *            the byte array to convert
	 * @return the conversion result or <code>null</code> if the conversion
	 *         fails.
	 */
	public static String toString(byte[] someBytes) {
		String result;
		try {
			result = new String(someBytes, ISO_8859_1);
			return result;
		} catch (UnsupportedEncodingException e) {
			logger.error("Can't convert byte[] to String:", e);
		}
		return null;
	}

	public static KeyPair generateRSAKeys() {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception during key creation:", e);
		}
		return null;
	}

	public static String convertKeyToString(Key aKey, boolean isPublic) {
		KeyFactory fact;
		try {
			BigInteger m, e;
			fact = KeyFactory.getInstance("RSA");
			if (isPublic) {
				RSAPublicKeySpec publicKeySpec = fact.getKeySpec(aKey, RSAPublicKeySpec.class);
				m = publicKeySpec.getModulus();
				e = publicKeySpec.getPublicExponent();
			} else {
				RSAPrivateKeySpec privateKeySpec = fact.getKeySpec(aKey, RSAPrivateKeySpec.class);
				m = privateKeySpec.getModulus();
				e = privateKeySpec.getPrivateExponent();
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append(Base64.encode(m.toByteArray()));
			buffer.append(DELIMITER);
			buffer.append(Base64.encode(e.toByteArray()));
			return buffer.toString();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Exception while extracting the information of the key:", e);
		}
		return null;
	}

	public static String encrypt(PrivateKey aPrivateKey, byte[] anEasKey) {
		return convertKeyToString(aPrivateKey, false);
	}

	public static Key convertStringToKey(String aString, boolean isPublic) {
		String[] parts = aString.split(DELIMITER);
		BigInteger m = new BigInteger(Base64.decode(parts[0]));
		BigInteger e = new BigInteger(Base64.decode(parts[1]));
		try {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			if (isPublic) {
				RSAPublicKeySpec pbuclicKeySpec = new RSAPublicKeySpec(m, e);
				return fact.generatePublic(pbuclicKeySpec);
			} else {
				RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(m, e);
				return fact.generatePrivate(privateKeySpec);
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
			logger.error("Exception while creating the key:", e1);
		}
		return null;
	}

	public static PrivateKey decryptPrivateKey(String aString, byte[] anEasKey) {
		return (PrivateKey) convertStringToKey(aString, false);
	}

	// public static ClosedUserProfile encrypt(User aUser) {
	// ClosedUserProfile closedUserProfile = ModelFactory.eINSTANCE.createClosedUserProfile();
	// closedUserProfile.setUserID(aUser.getUserID());
	// closedUserProfile.setPublicKey(convertKeyToString(aUser.getProfile().getPublicKey(), true));
	//
	// byte[] aesKey = createRandomAESKey();
	// SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
	// try {
	// Cipher cipher = Cipher.getInstance(AES_CIPHER_MODE);
	// try {
	// cipher.init(Cipher.ENCRYPT_MODE, keySpec);
	// try {
	// byte[] encodedPrivateKey = cipher.doFinal(toByte(convertKeyToString(aUser.getProfile()
	// .getPrivateKey(), false)));
	// closedUserProfile.setPrivateKey(toString(encodedPrivateKey));
	//
	// // encrypt file tree
	// Resource rs = ModelUtil.createResource("save");
	// rs.getContents().add(aUser.getProfile().getFileRoot());
	// String fileTreeAsString = ModelUtil.resourceToString(rs, UTF_8);
	// String encryptedFileTreeAsString = toString(cipher.doFinal(toByte(fileTreeAsString)));
	// closedUserProfile.setFileGhost(encryptedFileTreeAsString);
	//
	// // encrypt friend lists
	// ArrayList<Friend> friends = new ArrayList<Friend>(aUser.getProfile().getFriends());
	// closedUserProfile.setFriends(new EncryptionCapsule(friends, aUser.getProfile()
	// .getPublicKey()));
	// ArrayList<Friend> friendRequests = new ArrayList<Friend>(aUser.getProfile()
	// .getFriendRequests());
	// closedUserProfile.setFriendRequests(new EncryptionCapsule(friendRequests, aUser.getProfile()
	// .getPublicKey()));
	// ArrayList<Friend> pendingFriendRequests = new ArrayList<Friend>(aUser.getProfile()
	// .getPendingFriendRequests());
	// closedUserProfile.setPendingFriendRequests(new EncryptionCapsule(pendingFriendRequests, aUser
	// .getProfile().getPublicKey()));
	//
	// byte[] salt = createRandomSalt();
	// closedUserProfile.setTempKey(encryptTempKey(aesKey, aUser.getPassword(), salt));
	// closedUserProfile.setPrivateSalt(toString(salt));
	// closedUserProfile.setEPass(aUser.getProfile().getEPass());
	// closedUserProfile.setIv(toString(cipher.getIV()));
	// } catch (IllegalBlockSizeException | BadPaddingException e) {
	// logger.error("Exception during encryption:", e);
	// }
	// } catch (InvalidKeyException e) {
	// logger.error("Invalide key:", e);
	// e.printStackTrace();
	// }
	//
	// } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
	// logger.error("Error during cipher initialisation:", e);
	// }
	//
	// return closedUserProfile;
	// }

	// //@SuppressWarnings("unchecked")
	// public static UserProfile decrypt(ClosedUserProfile aClosedProfile, User aUser) {
	// UserProfile userProfile = ModelFactory.eINSTANCE.createUserProfile();
	//
	// byte[] aesKey = decryptTempKey(aClosedProfile.getTempKey(), aUser.getPassword(),
	// aClosedProfile.getPrivateSalt());
	// SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
	// try {
	// Cipher c = Cipher.getInstance(AES_CIPHER_MODE);
	// try {
	// c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(toByte(aClosedProfile.getIv())));
	// try {
	// // decrypt private key
	// String privateKey = toString(c.doFinal(toByte(aClosedProfile.getPrivateKey())));
	// userProfile.setPrivateKey((PrivateKey) convertStringToKey(privateKey, false));
	// userProfile.setPublicKey((PublicKey) convertStringToKey(aClosedProfile.getPublicKey(),
	// true));
	// userProfile.setEPass(aClosedProfile.getEPass());
	//
	// // decrypt file tree
	// String decryptedFileTreeAsString = toString(c.doFinal(toByte(aClosedProfile
	// .getFileGhost())));
	// Resource fileTreeResource = ModelUtil.stringToResource(decryptedFileTreeAsString, UTF_8);
	// Directory fileTreeRoot = (Directory) fileTreeResource.getContents().get(0);
	// userProfile.setFileRoot(fileTreeRoot);
	//
	// // decrypt friend lists
	// userProfile.getFriends().addAll(
	// (ArrayList<Friend>) aClosedProfile.getFriends().getContent(
	// userProfile.getPrivateKey()));
	// userProfile.getFriendRequests().addAll(
	// (ArrayList<Friend>) aClosedProfile.getFriendRequests().getContent(
	// userProfile.getPrivateKey()));
	// userProfile.getPendingFriendRequests().addAll(
	// (ArrayList<Friend>) aClosedProfile.getPendingFriendRequests().getContent(
	// userProfile.getPrivateKey()));
	// } catch (IllegalBlockSizeException | BadPaddingException e) {
	// logger.error("Exception during encryption:", e);
	// }
	// } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
	// logger.error("Exception during cipher initialisation:", e);
	// }
	//
	// } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
	// logger.error("Error during cipher initialisation:", e);
	// }
	//
	// return userProfile;
	// }

	private static String encryptTempKey(byte[] aesKey, String aPassword, byte[] someSalt) {
		try {
			SecretKey desKey = createDESKey(aPassword, someSalt);
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, desKey);
			return toString(cipher.doFinal(aesKey));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Exception during temp key encryption:", e);
		}
		return null;
	}

	private static byte[] decryptTempKey(String aKeyAsString, String aPassword, String someSalt) {
		try {
			SecretKey desKey = createDESKey(aPassword, toByte(someSalt));
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, desKey);
			return cipher.doFinal(toByte(aKeyAsString));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Exception during temp key decryption:", e);
		}
		return null;
	}

	private static byte[] createRandomSalt() {
		Random r = new SecureRandom();
		byte[] salt = new byte[20];
		r.nextBytes(salt);
		return salt;
	}

	private static SecretKey createDESKey(String aPassword, byte[] someSalt) {

		byte[] tempKey = combine(toByte(aPassword), someSalt);

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e1) {
			logger.error("Exception  during digest creation:", e1);
			return null;
		}
		for (int i = 0; i < 1024; i++) {
			md.update(tempKey);
			tempKey = md.digest();
			tempKey = combine(someSalt, tempKey);
		}

		try {
			DESKeySpec dks = new DESKeySpec(tempKey);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			SecretKey desKey = skf.generateSecret(dks);
			return desKey;
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Exception during des key creation from password:", e);
		}
		return null;
	}

	private static byte[] combine(byte[] arrayA, byte[] arrayB) {
		byte[] result = new byte[arrayA.length + arrayB.length];
		System.arraycopy(arrayA, 0, result, 0, arrayA.length);
		System.arraycopy(arrayB, 0, result, arrayA.length, arrayB.length);
		return result;
	}

	// public static String createEPassFromPassword(String aPassword) {
	// Digester digester = new Digester();
	// digester.setAlgorithm("SHA-1");
	// byte[] digest = digester.digest(EncryptionUtil.toByte(aPassword));
	// return EncryptionUtil.toString(digest);
	// }

	public static void encryptFileRSA(Path aSourcePath, Path aTargetPath, PublicKey aPublicKey)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		encryptOrDecryptRSA(aPublicKey, Cipher.ENCRYPT_MODE, aSourcePath, aTargetPath);
	}

	public static void decryptFileRSA(Path anInputPath, Path anOutputPath, PrivateKey aPrivateKey)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		encryptOrDecryptRSA(aPrivateKey, Cipher.DECRYPT_MODE, anInputPath, anOutputPath);
	}

	private static void encryptOrDecryptRSA(Key aKey, int aMode, Path anInputPath, Path anOutputPath)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
		FileInputStream fis = new FileInputStream(anInputPath.toFile());
		FileOutputStream fos = new FileOutputStream(anOutputPath.toFile());
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(aMode, aKey);
		if (aMode == Cipher.ENCRYPT_MODE) {
			CipherInputStream cis = new CipherInputStream(fis, cipher);
			doCopy(cis, fos);
		} else if (aMode == Cipher.DECRYPT_MODE) {
			CipherOutputStream cos = new CipherOutputStream(fos, cipher);
			doCopy(fis, cos);
		}
	}

	private static void doCopy(InputStream is, OutputStream os) throws IOException {
		byte[] bytes = new byte[64];
		int numBytes;
		while ((numBytes = is.read(bytes)) != -1) {
			os.write(bytes, 0, numBytes);
		}
		os.flush();
		os.close();
		is.close();
	}

	/**
	 * Creates a SHA-256 checksum for a file.
	 * 
	 * @param aFilePaht the path of the file.
	 * @return the checksum as a {@link String} or <code>null</code> if an exception occured.
	 */
	public static String generateSHACheckSumForFile(Path aFilePaht) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			try {
				FileInputStream fis = new FileInputStream(aFilePaht.toFile());
				byte[] dataBytes = new byte[1024];

				int nread = 0;
				try {
					while ((nread = fis.read(dataBytes)) != -1) {
						md.update(dataBytes, 0, nread);
					}
					fis.close();

					byte[] mdbytes = md.digest();

					StringBuffer hexString = new StringBuffer();
					for (int i = 0; i < mdbytes.length; i++) {
						hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
					}
					return hexString.toString();
				} catch (IOException e) {
					logger.error("Exception while reading file:", e);
				}
			} catch (FileNotFoundException e) {
				logger.error("Can't create a FileInputStream from path '" + aFilePaht.toString() + "'", e);
			}
		} catch (NoSuchAlgorithmException e) {
			logger.error("Can't generate a MessageDigest for 'SHA-256'!", e);
		}
		return null;
	}

	public static Object deserializeObject(String anObjectAsString) {
		byte[] data = Base64.decode(anObjectAsString);
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (IOException | ClassNotFoundException e) {
			logger.error(String.format("Exception while deserializing object '%s':", anObjectAsString), e);
		}
		return null;
	}

	/** Write the object to a Base64 string. */
	public static String serializeObject(Serializable aSerializableObject) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(aSerializableObject);
			oos.close();
			return new String(Base64.encode(baos.toByteArray()));
		} catch (IOException e) {
			logger.error(
					String.format("Exception while serializing object '%s'", aSerializableObject.toString()),
					e);
		}
		return null;
	}

	public static String serializePath(Path path) {
		if (path.getNameCount() < 2) {
			return path.toString();
		} else {
			StringBuffer buffer = new StringBuffer(path.getName(0).toString());
			for (int i = 1; i < path.getNameCount(); i++) {
				buffer.append(EncryptionUtil.DELIMITER);
				buffer.append(path.getName(i).toString());
			}
			return buffer.toString();
		}
	}

	public static Path deserializePath(String serializedPath) {
		String[] pathParts = serializedPath.split(EncryptionUtil.DELIMITER);
		if (pathParts.length > 1) {
			String tail[] = new String[pathParts.length - 1];
			System.arraycopy(pathParts, 1, tail, 0, pathParts.length - 1);
			return Paths.get("", pathParts);
			// return Paths.get(pathParts[0], tail);
		} else if (pathParts.length == 1) {
			return Paths.get(pathParts[0]);
		}
		return null;
	}

}
