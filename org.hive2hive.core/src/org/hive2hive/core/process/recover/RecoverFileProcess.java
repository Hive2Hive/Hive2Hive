package org.hive2hive.core.process.recover;

import java.io.File;
import java.io.FileNotFoundException;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.File2MetaFileStep;

/**
 * Recover a specific file version. First, all versions of the specific files are fetched, the user then can
 * decide which version he wants to restore.
 * 
 * @author Nico
 * 
 */
public class RecoverFileProcess extends Process {

	private final RecoverFileProcessContext context;

	public RecoverFileProcess(NetworkManager networkManager, File file, IVersionSelector versionSelector)
			throws NoSessionException, FileNotFoundException {
		super(networkManager);
		context = new RecoverFileProcessContext(this, file, versionSelector);

		if (file.isDirectory()) {
			throw new IllegalArgumentException("A foler has only one version");
		} else if (!file.exists()) {
			throw new FileNotFoundException("File does not exist");
		}

		// TODO check if file is in H2H directory: use a common method for that!

		H2HSession session = networkManager.getSession();
		ProcessStep firstStep = new File2MetaFileStep(file, session.getProfileManager(),
				session.getFileManager(), context, new SelectVersionStep());
		setNextStep(firstStep);
	}

	@Override
	public RecoverFileProcessContext getContext() {
		return context;
	}
}
