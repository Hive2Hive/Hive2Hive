package org.hive2hive.core.encryption;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.emf.ecore.xml.type.internal.DataValue.Base64;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class FileEncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileEncryptionUtil.class);
	
	private static final String DELIMITER = "DELIMITERDELIMITERDELIMITERDELIMITER";
	
	private FileEncryptionUtil() {
	}
	
	public static String serializePath(Path path) {
		if (path.getNameCount() < 2) {
			return path.toString();
		} else {
			StringBuffer buffer = new StringBuffer(path.getName(0).toString());
			for (int i = 1; i < path.getNameCount(); i++) {
				buffer.append(DELIMITER);
				buffer.append(path.getName(i).toString());
			}
			return buffer.toString();
		}
	}

	public static Path deserializePath(String serializedPath) {
		String[] pathParts = serializedPath.split(DELIMITER);
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
	
	public static String encryptPrivateKey(PrivateKey aPrivateKey, byte[] anEasKey) {
		return convertKeyToString(aPrivateKey, false);
	}

	public static PrivateKey decryptPrivateKey(String aString, byte[] anEasKey) {
		return (PrivateKey) convertStringToKey(aString, false);
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
		//	buffer.append(DELIMITER);
			buffer.append(Base64.encode(e.toByteArray()));
			return buffer.toString();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Exception while extracting the information of the key:", e);
		}
		return null;
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

}
