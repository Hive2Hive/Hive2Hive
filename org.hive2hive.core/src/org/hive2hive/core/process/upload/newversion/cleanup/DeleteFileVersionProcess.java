package org.hive2hive.core.process.upload.newversion.cleanup;

import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * Delete a specific version of a file. This process assumes that the given version is already removed from
 * the meta file
 * 
 * @author Nico
 * 
 */
public class DeleteFileVersionProcess extends Process {

	public DeleteFileVersionProcess(NetworkManager networkManager, FileVersion version) {
		super(networkManager);
		new DeleteChunkStep(version.getChunkIds());
	}
}
