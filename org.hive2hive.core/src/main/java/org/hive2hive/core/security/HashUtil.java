package org.hive2hive.core.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util for hashing and comparing hashes.
 * TODO: this could be parameterized with the security provider for an optimal result
 * 
 * @author Nico
 * @author Chris
 * 
 */
public class HashUtil {

	private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);
	private static final String HASH_ALGORITHM = "MD5";

	private HashUtil() {
		// only static methods
	}

	/**
	 * Generates a MD5 hash of a given data
	 * 
	 * @param data to calculate the MD5 hash over it
	 * @return the md5 hash
	 */
	public static byte[] hash(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			digest.update(data, 0, data.length);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Invalid hash algorithm {}", HASH_ALGORITHM, e);
			return new byte[0];
		}
	}

	/**
	 * Generates a MD5 hash of an input stream (can take a while)
	 * 
	 * @param file
	 * @return the hash of the file
	 * @throws IOException
	 */
	public static byte[] hash(File file) throws IOException {
		if (file == null) {
			return new byte[0];
		} else if (file.isDirectory()) {
			return new byte[0];
		} else if (!file.exists()) {
			return new byte[0];
		}

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Invalid hash algorithm {}", HASH_ALGORITHM, e);
			return new byte[0];
		}

		byte[] buffer = new byte[1024];
		int numRead;
		FileInputStream fis;

		try {
			// open the stream
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			logger.error("File {} not found to generate the hash", file, e);
			return new byte[0];
		}

		DigestInputStream dis = new DigestInputStream(fis, digest);
		do {
			numRead = dis.read(buffer);
			if (numRead > 0) {
				digest.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		dis.close();
		fis.close();

		return digest.digest();
	}

	/**
	 * Compares if the file md5 matches a given md5 hash
	 * 
	 * @param file
	 * @param expectedMD5
	 * @return <code>true</code> if the file has the expected hash
	 * @throws IOException
	 */
	public static boolean compare(File file, byte[] expectedMD5) throws IOException {
		if (!file.exists() && (expectedMD5 == null || expectedMD5.length == 0)) {
			// both do not exist
			return true;
		} else if (file.isDirectory()) {
			// directories always match
			return true;
		}

		byte[] md5Hash = HashUtil.hash(file);
		return compare(md5Hash, expectedMD5);
	}

	/**
	 * Compares if the given md5 matches another md5 hash. This method works symmetrically and is not
	 * dependent on the parameter order
	 * 
	 * @param md5 the hash to test
	 * @param expectedMD5 the expected md5 hash
	 * @return <code>true</code> if the hashes match
	 */
	public static boolean compare(byte[] md5, byte[] expectedMD5) {
		return Arrays.equals(md5, expectedMD5);
	}
}
