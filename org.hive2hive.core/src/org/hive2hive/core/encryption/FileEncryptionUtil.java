package org.hive2hive.core.encryption;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * This class provides special encryption and decryption functionalities for files.
 * 
 * @author Christian
 * 
 */
public final class FileEncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileEncryptionUtil.class);

	private static final String DELIMITER = "DELIMITERDELIMITER";

	private FileEncryptionUtil() {
	}

	/**
	 * Generates a SHA-256 checksum based on the binary representation of a file.
	 * 
	 * @param filePath The path of the file which shall be check summed.
	 * @return The checksum of the file.
	 * @throws IOException
	 */
	public static String generateChecksum(Path filePath) throws IOException {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			FileInputStream fis = new FileInputStream(filePath.toFile());

			int position = 0;
			byte[] buffer = new byte[1024];
			while ((position = fis.read(buffer)) != 1) {
				digest.update(buffer, 0, position);
			}
			fis.close();
			byte[] digestBytes = digest.digest();

			return EncryptionUtil.toHex(digestBytes);

		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception while creating checksum;", e);
		}

		return null;

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
		} else if (pathParts.length == 1) {
			return Paths.get(pathParts[0]);
		}
		return null;
	}

	private static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {

		int numBytes;
		byte[] buffer = new byte[64];
		while ((numBytes = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, numBytes);
		}
		outputStream.flush();
		outputStream.close();
		inputStream.close();
	}
}
