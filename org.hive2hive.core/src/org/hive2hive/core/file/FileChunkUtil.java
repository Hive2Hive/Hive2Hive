package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.hive2hive.core.model.Chunk;

public class FileChunkUtil {

	private FileChunkUtil() {
		// only static methods
	}

	/**
	 * Calculates the number of chunks. This depends on the file size and the chunk size
	 * 
	 * @param file the file to chunk
	 * @param chunkSize the size of an individual chunk
	 * @return the number of chunks
	 */
	public static int getNumberOfChunks(File file, int chunkSize) {
		long fileSize = FileUtil.getFileSize(file);
		return (int) Math.ceil((double) fileSize / chunkSize);
	}

	/**
	 * Returns the chunk of a given file.
	 * 
	 * @param file the file to chunk
	 * @param chunkSize the maximum size of a single chunk. If the end of the file has been reached before,
	 *            the returned chunk can be smaller.
	 * @param chunkNumber the index of the chunk, starting at 0. When giving 0, the first chunk is read. This
	 *            parameter is similar to the offset.
	 * @param chunkId the id of the chunk which should be returned
	 * @return the chunk or null if no data could be read with the given parameter
	 * @throws IOException if the file cannot be read
	 */
	public static Chunk getChunk(File file, int chunkSize, int chunkNumber, String chunkId)
			throws IOException {
		int read = 0;
		long offset = chunkSize * chunkNumber;
		byte[] data = new byte[chunkSize];

		// read the next chunk of the file considering the offset
		RandomAccessFile rndAccessFile = new RandomAccessFile(file, "r");
		rndAccessFile.seek(offset);
		read = rndAccessFile.read(data);
		rndAccessFile.close();

		if (read > 0) {
			// the byte-Array may contain many empty slots if last chunk. Truncate it
			data = truncateData(data, read);
			return new Chunk(chunkId, data, chunkNumber, read);
		} else {
			return null;
		}
	}

	/**
	 * Truncates a byte array
	 * 
	 * @param data
	 * @param read
	 * @return a shorter byte array
	 */
	private static byte[] truncateData(byte[] data, int numOfBytes) {
		// shortcut
		if (data.length == numOfBytes) {
			return data;
		} else {
			byte[] truncated = new byte[numOfBytes];
			for (int i = 0; i < truncated.length; i++) {
				truncated[i] = data[i];
			}
			return truncated;
		}
	}
}
