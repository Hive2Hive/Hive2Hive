package org.hive2hive.core.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.files.list.FileTaste;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.core.processes.files.util.FileRecursionUtil;
import org.hive2hive.core.processes.files.util.FileRecursionUtil.FileProcessAction;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.decorators.AsyncResultComponent;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IResultProcessComponent;

/**
 * Default implementation of {@link IFileManager}.
 * 
 * @author Christian, Nico
 * 
 */
public class H2HFileManager extends H2HManager implements IFileManager {

	public H2HFileManager(NetworkManager networkManager) {
		super(networkManager);
	}

	@Override
	public IProcessComponent synchronize() throws NoSessionException {
		if (networkManager.getSession() == null) {
			throw new NoSessionException();
		}

		IProcessComponent syncProcess = ProcessFactory.instance().createSynchronizeFilesProcess(networkManager);
		AsyncComponent asyncProcess = new AsyncComponent(syncProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent add(File file) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation {
		// verify the argument
		H2HSession session = networkManager.getSession();
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null.");
		} else if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist.");
		} else if (session.getRoot().toFile().equals(file)) {
			throw new IllegalArgumentException("Root cannot be added.");
		} else if (!FileUtil.isInH2HDirectory(file, session)) {
			throw new IllegalFileLocation();
		}

		IProcessComponent addProcess;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// add the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			addProcess = FileRecursionUtil.buildUploadProcess(preorderList, FileProcessAction.NEW_FILE, networkManager);
		} else {
			// add single file
			addProcess = ProcessFactory.instance().createNewFileProcess(file, networkManager);
		}

		AsyncComponent asyncProcess = new AsyncComponent(addProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent update(File file) throws NoSessionException, NoPeerConnectionException {
		if (file.isDirectory()) {
			throw new IllegalArgumentException("A folder can have one version only");
		} else if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist");
		} else if (!FileUtil.isInH2HDirectory(file, networkManager.getSession())) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		}

		IProcessComponent updateProcess = ProcessFactory.instance().createUpdateFileProcess(file, networkManager);
		AsyncComponent asyncProcess = new AsyncComponent(updateProcess);

		submitProcess(asyncProcess);
		return asyncProcess;

	}

	@Override
	public IProcessComponent move(File source, File destination) throws NoSessionException, NoPeerConnectionException {
		// TODO support the file listener that already moved the file
		if (!source.exists()) {
			throw new IllegalArgumentException("Source file not found");
		} else if (destination.exists()) {
			throw new IllegalArgumentException("Destination already exists");
		} else if (!FileUtil.isInH2HDirectory(source, networkManager.getSession())) {
			throw new IllegalArgumentException("Source file not in the Hive2Hive directory");
		} else if (!FileUtil.isInH2HDirectory(destination, networkManager.getSession())) {
			throw new IllegalArgumentException("Destination file not in the Hive2Hive directory");
		}

		IProcessComponent moveProcess = ProcessFactory.instance().createMoveFileProcess(source, destination, networkManager);

		AsyncComponent asyncProcess = new AsyncComponent(moveProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent delete(File file) throws NoSessionException, NoPeerConnectionException {
		if (!FileUtil.isInH2HDirectory(file, networkManager.getSession())) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		}

		IProcessComponent deleteProcess;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// delete the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			deleteProcess = FileRecursionUtil.buildDeletionProcess(preorderList, networkManager);
		} else {
			// delete a single file
			deleteProcess = ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
		}

		AsyncComponent asyncProcess = new AsyncComponent(deleteProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent recover(File file, IVersionSelector versionSelector) throws FileNotFoundException,
			NoSessionException, NoPeerConnectionException {
		// do some verifications
		if (file.isDirectory()) {
			throw new IllegalArgumentException("A foler has only one version");
		} else if (!file.exists()) {
			throw new FileNotFoundException("File does not exist");
		}

		IProcessComponent recoverProcess = ProcessFactory.instance().createRecoverFileProcess(file, versionSelector,
				networkManager);

		AsyncComponent asyncProcess = new AsyncComponent(recoverProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent share(File folder, String userId, PermissionType permission) throws IllegalFileLocation,
			NoSessionException, NoPeerConnectionException {
		// verify
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("File has to be a folder.");
		}
		if (!folder.exists()) {
			throw new IllegalFileLocation("Folder does not exist.");
		}

		H2HSession session = networkManager.getSession();
		Path root = session.getRoot();

		// folder must be in the given root directory
		if (!folder.toPath().toString().startsWith(root.toString())) {
			throw new IllegalFileLocation("Folder must be in root of the H2H directory.");
		}

		// sharing root folder is not allowed
		if (folder.toPath().toString().equals(root.toString())) {
			throw new IllegalFileLocation("Root folder of the H2H directory can't be shared.");
		}

		IProcessComponent shareProcess = ProcessFactory.instance().createShareProcess(folder,
				new UserPermission(userId, permission), networkManager);

		AsyncComponent asyncProcess = new AsyncComponent(shareProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IResultProcessComponent<List<FileTaste>> getFileList() throws NoSessionException {
		IResultProcessComponent<List<FileTaste>> fileListProcess = ProcessFactory.instance().createFileListProcess(
				networkManager);

		AsyncResultComponent<List<FileTaste>> asyncProcess = new AsyncResultComponent<List<FileTaste>>(fileListProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}
}