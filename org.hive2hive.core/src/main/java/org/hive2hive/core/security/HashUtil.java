package org.hive2hive.core.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.io.DigestInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashUtil {

	private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);

	/**
	 * Generates a MD5 hash of a given data
	 * 
	 * @param data to calculate the MD5 hash over it
	 * @return the md5 hash
	 */
	public static byte[] hash(byte[] data) {
		MD5Digest digest = new MD5Digest();
		digest.update(data, 0, data.length);
		byte[] md5 = new byte[digest.getDigestSize()];
		digest.doFinal(md5, 0);
		return md5;
	}

	/**
	 * Generates a MD5 hash of an input stream
	 * 
	 * @param stream
	 * @return
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

		MD5Digest digest = new MD5Digest();
		DigestInputStream dis = new DigestInputStream(fis, digest);
		do {
			numRead = dis.read(buffer);
			if (numRead > 0) {
				digest.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		dis.close();
		fis.close();

		byte[] md5 = new byte[digest.getDigestSize()];
		digest.doFinal(md5, 0);

		return md5;
	}
}
