package org.hive2hive.core.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.api.configs.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoNetworkException;
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

public class H2HFileManager extends H2HManager implements IFileManager {

	private final IFileConfiguration fileConfiguration;

	public H2HFileManager(IFileConfiguration fileConfiguration) {
		this.fileConfiguration = fileConfiguration;
	}

	@Override
	public IProcessComponent add(File file) throws NoSessionException, NoPeerConnectionException, NoNetworkException {

		IProcessComponent addProcess;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// add the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			addProcess = FileRecursionUtil.buildUploadProcess(preorderList, FileProcessAction.NEW_FILE, getNetworkManager());
		} else {
			// add single file
			addProcess = ProcessFactory.instance().createNewFileProcess(file, getNetworkManager());
		}

		submitProcess(addProcess);
		return addProcess;
	}

	@Override
	public IProcessComponent update(File file) throws NoSessionException, IllegalArgumentException,
			NoPeerConnectionException, NoNetworkException {

		IProcessComponent updateProcess = ProcessFactory.instance().createUpdateFileProcess(file,
				getNetworkManager());
		
		submitProcess(updateProcess);
		return updateProcess;

	}

	@Override
	public IProcessComponent move(File source, File destination) throws NoSessionException,
			NoPeerConnectionException, NoNetworkException {

		IProcessComponent moveProcess = ProcessFactory.instance().createMoveFileProcess(source, destination,
				getNetworkManager());

		submitProcess(moveProcess);
		return moveProcess;
	}

	@Override
	public IProcessComponent delete(File file) throws NoSessionException, NoPeerConnectionException, NoNetworkException {

		IProcessComponent deleteProcess;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// delete the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			deleteProcess = FileRecursionUtil.buildDeletionProcess(preorderList, getNetworkManager());
		} else {
			// delete a single file
			deleteProcess = ProcessFactory.instance().createDeleteFileProcess(file, getNetworkManager());
		}

		submitProcess(deleteProcess);
		return deleteProcess;
	}

	@Override
	public IProcessComponent recover(File file, IVersionSelector versionSelector)
			throws FileNotFoundException, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException, NoNetworkException {

		IProcessComponent recoverProcess = ProcessFactory.instance().createRecoverFileProcess(file,
				versionSelector, getNetworkManager());

		submitProcess(recoverProcess);
		return recoverProcess;
	}

	@Override
	public IProcessComponent share(File folder, String userId, PermissionType permission)
			throws IllegalFileLocation, IllegalArgumentException, NoSessionException,
			NoPeerConnectionException, NoNetworkException {

		IProcessComponent shareProcess = ProcessFactory.instance().createShareProcess(folder,
				new UserPermission(userId, permission), getNetworkManager());

		submitProcess(shareProcess);
		return shareProcess;
	}

	@Override
	public IResultProcessComponent<List<Path>> getFileList() throws NoNetworkException {

		IResultProcessComponent<List<Path>> fileListProcess = ProcessFactory.instance()
				.createFileListProcess(getNetworkManager());

		submitProcess(fileListProcess);
		return fileListProcess;
	}

	@Override
	public IFileConfiguration getFileConfiguration() {
		return fileConfiguration;
	}

}
