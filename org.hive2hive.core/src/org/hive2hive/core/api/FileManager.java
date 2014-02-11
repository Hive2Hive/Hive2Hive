package org.hive2hive.core.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.api.interfaces.INetworkConfiguration;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.files.recover.IVersionSelector;
import org.hive2hive.core.processes.implementations.files.util.FileRecursionUtil;
import org.hive2hive.core.processes.implementations.files.util.FileRecursionUtil.FileProcessAction;

public class FileManager extends NetworkNode implements IFileManager {

	private final IFileConfiguration fileConfiguration;
	private final ProcessManager processManager;

	public FileManager(INetworkConfiguration networkConfiguration, IFileConfiguration fileConfiguration,
			ProcessManager processManager) {
		super(networkConfiguration);
		this.fileConfiguration = fileConfiguration;
		this.processManager = processManager;
	}

	@Override
	public IProcessComponent add(File file) throws NoSessionException, NoPeerConnectionException {

		IProcessComponent addProcess;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// add the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			addProcess = FileRecursionUtil.buildUploadProcess(preorderList, FileProcessAction.NEW_FILE,
					networkManager);
		} else {
			// add single file
			addProcess = ProcessFactory.instance().createNewFileProcess(file, networkManager);
		}

		processManager.submit(addProcess);
		return addProcess;
	}

	@Override
	public IProcessComponent update(File file) throws NoSessionException, IllegalArgumentException,
			NoPeerConnectionException {

		IProcessComponent updateProcess = ProcessFactory.instance().createUpdateFileProcess(file,
				networkManager);

		processManager.submit(updateProcess);
		return updateProcess;

	}

	@Override
	public IProcessComponent move(File source, File destination) throws NoSessionException,
			NoPeerConnectionException {

		IProcessComponent moveProcess = ProcessFactory.instance().createMoveFileProcess(source, destination,
				networkManager);

		processManager.submit(moveProcess);
		return moveProcess;
	}

	@Override
	public IProcessComponent delete(File file) throws NoSessionException, NoPeerConnectionException {

		IProcessComponent deleteProcess;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// delete the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			deleteProcess = FileRecursionUtil.buildDeletionProcess(preorderList, networkManager);
		} else {
			// delete a single file
			deleteProcess = ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
		}

		processManager.submit(deleteProcess);
		return deleteProcess;
	}

	@Override
	public IProcessComponent recover(File file, IVersionSelector versionSelector)
			throws FileNotFoundException, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {

		IProcessComponent recoverProcess = ProcessFactory.instance().createRecoverFileProcess(file,
				versionSelector, networkManager);

		processManager.submit(recoverProcess);
		return recoverProcess;
	}

	@Override
	public IProcessComponent share(File folder, String userId, PermissionType permission)
			throws IllegalFileLocation, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException {

		IProcessComponent shareProcess = ProcessFactory.instance().createShareProcess(folder,
				new UserPermission(userId, permission), networkManager);

		processManager.submit(shareProcess);
		return shareProcess;
	}

	@Override
	public IResultProcessComponent<List<Path>> getFileList() {

		IResultProcessComponent<List<Path>> fileListProcess = ProcessFactory.instance()
				.createFileListProcess(networkManager);
		
		processManager.submit(fileListProcess);
		return fileListProcess;
	}

	@Override
	public IFileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}

}
