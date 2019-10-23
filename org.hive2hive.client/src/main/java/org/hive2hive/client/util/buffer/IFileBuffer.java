package org.hive2hive.client.util.buffer;

import java.io.File;

public interface IFileBuffer {

	// how long the buffer collects incoming events until the buffer is processed
	public static final long BUFFER_WAIT_TIME_MS = 3000;

	/**
	 * The file is added to a buffer which waits a certain time. This ensures that the listener is not called
	 * for both parent folder and containing files, causing race-conditions at Hive2Hive.
	 * 
	 * @param file the file to add to the buffer
	 */
	void addFileToBuffer(File file);
}
