package org.hive2hive.core.process.move;

import java.io.File;

import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

/**
 * Moves the file to a new destination. The following steps are required: <br>
 * <ol>
 * <li>Move the file on disk</li>
 * <li>Get the source and the destinations parent meta folder</li>
 * <li>Remove the file from the former parent</li>
 * <li>Add the file from the new parent</li>
 * <li>Update the user profile</li>
 * <li>Notify other clients</li>
 * </ol>
 * 
 * @author Nico
 * 
 */
public class MoveFileProcess extends Process {

	static final H2HLogger logger = H2HLoggerFactory.getLogger(MoveFileProcess.class);

	private final MoveFileProcessContext context;

	public MoveFileProcess(NetworkManager networkManager, File source, File destination)
			throws IllegalArgumentException, NoSessionException {
		super(networkManager);
		context = new MoveFileProcessContext(this, source, destination);

		// verify the file
		verifyFiles(source, destination);

		logger.info(String.format("Moving a file '%s' from '%s' to '%s'.", source.getName(),
				source.getPath(), destination.getPath()));

		setNextStep(new MoveOnDiskStep());
	}

	private void verifyFiles(File source, File destination) throws IllegalArgumentException,
			NoSessionException {
		if (source.equals(destination)) {
			throw new IllegalArgumentException("Source and destination are the same");
		}

		if (!source.exists()) {
			throw new IllegalArgumentException("File to move does not exist");
		} else if (destination.exists()) {
			throw new IllegalArgumentException("Destination file already exists");
		}

		FileManager fileManager = getNetworkManager().getSession().getFileManager();
		if (!source.getAbsolutePath().startsWith(fileManager.getRoot().toString())) {
			throw new IllegalArgumentException("Source file is not in Hive2Hive directory. Use 'add'.");
		} else if (!destination.getAbsolutePath().startsWith(fileManager.getRoot().toString())) {
			throw new IllegalArgumentException("Destination file is not in Hive2Hive directory.");
		}
	}

	@Override
	public MoveFileProcessContext getContext() {
		return context;
	}
}
