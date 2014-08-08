package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
	 * @return the number of chunks, if the file is empty, 1 is returned. If file is not existing, 0 is
	 *         returned. In case the given chunkSize is smaller or equal to zero, 0 is returned
	 */
	public static int getNumberOfChunks(File file, int chunkSize) {
		if (file == null || !file.exists()) {
			// no chunk needed
			return 0;
		} else if (chunkSize <= 0) {
			// don't divide by 0
			return 0;
		}

		long fileSize = FileUtil.getFileSize(file);
		if (fileSize == 0) {
			// special case
			return 1;
		}

		return (int) Math.ceil((double) fileSize / Math.abs(chunkSize));
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
		if (file == null || !file.exists()) {
			throw new IOException("File does not exist");
		} else if (chunkSize <= 0) {
			throw new IOException("Chunk size cannot be smaller or equal to 0");
		} else if (chunkNumber < 0) {
			throw new IOException("Chunk number cannot be smaller than 0");
		}

		if (FileUtil.getFileSize(file) == 0 && chunkNumber == 0) {
			// special case: file exists but is empty.
			// return an empty chunk
			return new Chunk(chunkId, new byte[0], 0);
		}

		int read = 0;
		long offset = chunkSize * (long)chunkNumber;
		byte[] data = new byte[chunkSize];

		// read the next chunk of the file considering the offset
		RandomAccessFile rndAccessFile = new RandomAccessFile(file, "r");
		rndAccessFile.seek(offset);
		read = rndAccessFile.read(data);
		rndAccessFile.close();

		if (read > 0) {
			// the byte-Array may contain many empty slots if last chunk. Truncate it
			data = truncateData(data, read);
			return new Chunk(chunkId, data, chunkNumber);
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

	/**
	 * Reassembly of multiple file parts to a single file. Note that the file parts need to be sorted
	 * beforehand
	 * 
	 * @param fileParts the sorted file parts.
	 * @param destination the destination
	 * @param removeParts whether to remove copied file parts
	 * @throws IOException in case the files could not be read or written.
	 */
	public static void reassembly(List<File> fileParts, File destination, boolean removeParts)
			throws IOException {
		if (fileParts == null || fileParts.isEmpty()) {
			// nothing to reassembly
			return;
		}

		if (destination == null) {
			// don't know where to reassembly
			return;
		}

		if (destination.exists()) {
			// overwrite
			destination.delete();
		}

		for (File filePart : fileParts) {
			// copy file parts to the new location, append
			FileUtils.writeByteArrayToFile(destination, FileUtils.readFileToByteArray(filePart), true);

			if (removeParts)
				filePart.delete();
		}
	}
}
