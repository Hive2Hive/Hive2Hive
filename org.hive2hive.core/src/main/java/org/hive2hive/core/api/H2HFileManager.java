package org.hive2hive.core.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.extras.FileRecursionUtil;
import org.hive2hive.core.extras.FileRecursionUtil.FileProcessAction;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.files.list.FileTaste;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Default implementation of {@link IFileManager}.
 * This implementation of {@link IFileManager} is asynchronous. Thus, the return types of the
 * {@link IProcessComponent}s uses {@link Future}s.
 * 
 * @author Christian, Nico
 * 
 */
public class H2HFileManager extends H2HManager implements IFileManager {

	public H2HFileManager(NetworkManager networkManager, EventBus eventBus) {
		super(networkManager, eventBus);
	}

	@Override
	public IProcessComponent<Future<Void>> add(File file) throws NoSessionException, NoPeerConnectionException,
			IllegalFileLocation {
		// verify the argument
		H2HSession session = networkManager.getSession();
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null.");
		} else if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist.");
		} else if (session.getRootFile().equals(file)) {
			throw new IllegalArgumentException("Root cannot be added.");
		} else if (!FileUtil.isInH2HDirectory(session.getFileAgent(), file)) {
			throw new IllegalFileLocation();
		}

		IProcessComponent<Void> addProcess;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// add the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			addProcess = FileRecursionUtil.buildUploadProcess(preorderList, FileProcessAction.NEW_FILE, networkManager);
		} else {
			// add single file
			addProcess = ProcessFactory.instance().createNewFileProcess(file, networkManager);
		}

		IProcessComponent<Future<Void>> asyncProcess = new AsyncComponent<>(addProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent<Future<Void>> delete(File file) throws NoSessionException, NoPeerConnectionException {
		if (!FileUtil.isInH2HDirectory(networkManager.getSession().getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		}

		IProcessComponent<Future<Void>> asyncDeleteProcess;
		if (file.isDirectory() && file.listFiles().length > 0) {
			// delete the files recursively
			List<Path> preorderList = FileRecursionUtil.getPreorderList(file.toPath());
			asyncDeleteProcess = FileRecursionUtil.buildDeletionProcess(preorderList, networkManager);
			
			// built process is already async
			
		} else {
			// delete a single file
			IProcessComponent<Void> deleteProcess = ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
			
			// this process must be wrapped to be async
			asyncDeleteProcess = new AsyncComponent<>(deleteProcess);
		}

		submitProcess(asyncDeleteProcess);
		return asyncDeleteProcess;
	}

	@Override
	public IProcessComponent<Future<Void>> update(File file) throws NoSessionException, NoPeerConnectionException {
		if (file.isDirectory()) {
			throw new IllegalArgumentException("A folder can have one version only");
		} else if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist");
		} else if (!FileUtil.isInH2HDirectory(networkManager.getSession().getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		}

		IProcessComponent<Void> updateProcess = ProcessFactory.instance().createUpdateFileProcess(file, networkManager);
		IProcessComponent<Future<Void>> asyncProcess = new AsyncComponent<>(updateProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent<Future<Void>> move(File source, File destination) throws NoSessionException,
			NoPeerConnectionException {
		IFileAgent fileAgent = networkManager.getSession().getFileAgent();

		// TODO support the file listener that already moved the file
		if (!source.exists()) {
			// throw new IllegalArgumentException("Source file not found");
		} else if (destination.exists()) {
			throw new IllegalArgumentException("Destination already exists");
		} else if (!FileUtil.isInH2HDirectory(fileAgent, source)) {
			throw new IllegalArgumentException("Source file not in the Hive2Hive directory");
		} else if (!FileUtil.isInH2HDirectory(fileAgent, destination)) {
			throw new IllegalArgumentException("Destination file not in the Hive2Hive directory");
		}

		IProcessComponent<Void> moveProcess = ProcessFactory.instance().createMoveFileProcess(source, destination,
				networkManager);
		IProcessComponent<Future<Void>> asyncProcess = new AsyncComponent<>(moveProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent<Future<Void>> recover(File file, IVersionSelector versionSelector)
			throws FileNotFoundException, NoSessionException, NoPeerConnectionException {
		// do some verifications
		if (file.isDirectory()) {
			throw new IllegalArgumentException("A foler has only one version");
		} else if (!file.exists()) {
			throw new FileNotFoundException("File does not exist");
		}

		IProcessComponent<Void> recoverProcess = ProcessFactory.instance().createRecoverFileProcess(file, versionSelector,
				networkManager);
		IProcessComponent<Future<Void>> asyncProcess = new AsyncComponent<>(recoverProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent<Future<Void>> share(File folder, String userId, PermissionType permission)
			throws IllegalFileLocation, NoSessionException, NoPeerConnectionException {
		// verify
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("File has to be a folder.");
		}
		if (!folder.exists()) {
			throw new IllegalFileLocation("Folder does not exist.");
		}

		H2HSession session = networkManager.getSession();

		// folder must be in the given root directory
		if (!FileUtil.isInH2HDirectory(session.getFileAgent(), folder)) {
			throw new IllegalFileLocation("Folder must be in root of the H2H directory.");
		}

		// sharing root folder is not allowed
		if (folder.equals(session.getRootFile())) {
			throw new IllegalFileLocation("Root folder of the H2H directory can't be shared.");
		}

		IProcessComponent<Void> shareProcess = ProcessFactory.instance().createShareProcess(folder,
				new UserPermission(userId, permission), networkManager);
		IProcessComponent<Future<Void>> asyncProcess = new AsyncComponent<>(shareProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public IProcessComponent<Future<List<FileTaste>>> getFileList() throws NoSessionException {

		IProcessComponent<List<FileTaste>> fileListProcess = ProcessFactory.instance().createFileListProcess(networkManager);
		IProcessComponent<Future<List<FileTaste>>> asyncProcess = new AsyncComponent<>(fileListProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public void subscribeFileEvents(IFileEventListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("The argument listener must not be null.");
		}
		if (eventBus == null) {
			throw new IllegalStateException("No EventBus instance provided.");
		}
		eventBus.subscribe(listener);
	}
}