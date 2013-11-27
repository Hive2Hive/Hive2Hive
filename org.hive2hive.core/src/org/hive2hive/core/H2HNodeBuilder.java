package org.hive2hive.core;

/**
 * A builder class for building specific types of {@link IH2HNode}s.
 * 
 * @author Christian
 * 
 */
public class H2HNodeBuilder {

	/**
	 * Build a default {@link H2HNode} with the default values specified in {@link H2HConstants}.
	 * 
	 * @return default {@link H2HNode}
	 */
	public static H2HNode buildDefault() {
		return new H2HNode(H2HConstants.DEFAULT_MAX_FILE_SIZE, H2HConstants.DEFAULT_MAX_NUM_OF_VERSIONS,
				H2HConstants.DEFAULT_MAX_SIZE_ALL_VERSIONS, H2HConstants.DEFAULT_CHUNK_SIZE,
				H2HConstants.DEFAULT_AUTOSTART_PROCESSES, H2HConstants.DEFAULT_IS_MASTER_PEER,
				H2HConstants.DEFAULT_BOOTSTRAP_ADDRESS, H2HConstants.DEFAULT_ROOT_PATH);
	}
}
