package org.hive2hive.core.api;

import java.io.File;
import java.util.List;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.events.EventBus;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
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
 * 
 * @author Christian, Nico
 * 
 */
public class H2HFileManager extends H2HManager implements IFileManager {

	private final IFileConfiguration fileConfiguration;

	public H2HFileManager(NetworkManager networkManager, IFileConfiguration fileConfiguration, EventBus eventBus) {
		super(networkManager, eventBus);
		this.fileConfiguration = fileConfiguration;
	}

	@Override
	public AsyncComponent<Void> add(File file) throws NoSessionException, NoPeerConnectionException, IllegalFileLocation {
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

		IProcessComponent<Void> addProcess = ProcessFactory.instance().createAddFileProcess(file, networkManager,
				fileConfiguration);
		AsyncComponent<Void> asyncProcess = new AsyncComponent<Void>(addProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public AsyncComponent<Void> delete(File file) throws NoSessionException, NoPeerConnectionException {
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		} else if (!FileUtil.isInH2HDirectory(networkManager.getSession().getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		} else if (file.isDirectory() && file.list().length > 0) {
			throw new IllegalArgumentException("Folder to delete is not empty");
		}

		IProcessComponent<Void> deleteProcess = ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
		AsyncComponent<Void> asyncProcess = new AsyncComponent<Void>(deleteProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public AsyncComponent<Void> update(File file) throws NoSessionException, NoPeerConnectionException {
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		} else if (file.isDirectory()) {
			throw new IllegalArgumentException("A folder can have one version only");
		} else if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist");
		} else if (!FileUtil.isInH2HDirectory(networkManager.getSession().getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		}

		IProcessComponent<Void> updateProcess = ProcessFactory.instance().createUpdateFileProcess(file, networkManager,
				fileConfiguration);
		AsyncComponent<Void> asyncProcess = new AsyncComponent<Void>(updateProcess);

		submitProcess(asyncProcess);
		return asyncProcess;

	}

	@Override
	public AsyncComponent<Void> download(File file) throws NoSessionException, NoPeerConnectionException {
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		} else if (!FileUtil.isInH2HDirectory(networkManager.getSession().getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		}

		IProcessComponent<Void> downloadProcess = ProcessFactory.instance().createDownloadFileProcess(file, networkManager);
		AsyncComponent<Void> asyncProcess = new AsyncComponent<Void>(downloadProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public AsyncComponent<Void> move(File source, File destination) throws NoSessionException, NoPeerConnectionException {
		IFileAgent fileAgent = networkManager.getSession().getFileAgent();

		if (source == null) {
			throw new IllegalArgumentException("Source cannot be null");
		} else if (destination == null) {
			throw new IllegalArgumentException("Destination cannot be null");
		} else if (!FileUtil.isInH2HDirectory(fileAgent, source)) {
			throw new IllegalArgumentException("Source file not in the Hive2Hive directory");
		} else if (!FileUtil.isInH2HDirectory(fileAgent, destination)) {
			throw new IllegalArgumentException("Destination file not in the Hive2Hive directory");
		}

		IProcessComponent<Void> moveProcess = ProcessFactory.instance().createMoveFileProcess(source, destination,
				networkManager);
		AsyncComponent<Void> asyncProcess = new AsyncComponent<Void>(moveProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public AsyncComponent<Void> recover(File file, IVersionSelector versionSelector) throws NoSessionException,
			NoPeerConnectionException {
		// do some verifications
		if (file == null) {
			throw new IllegalArgumentException("File to recover cannot be null");
		} else if (file.isDirectory()) {
			throw new IllegalArgumentException("A folder has only one version");
		}

		IProcessComponent<Void> recoverProcess = ProcessFactory.instance().createRecoverFileProcess(file, versionSelector,
				networkManager);

		AsyncComponent<Void> asyncProcess = new AsyncComponent<Void>(recoverProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public AsyncComponent<Void> share(File folder, String userId, PermissionType permission) throws IllegalFileLocation,
			NoSessionException, NoPeerConnectionException {
		// verify
		if (folder == null) {
			throw new IllegalArgumentException("Folder to share cannot be null");
		} else if (!folder.isDirectory()) {
			throw new IllegalArgumentException("File has to be a folder.");
		} else if (!folder.exists()) {
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

		AsyncComponent<Void> asyncProcess = new AsyncComponent<Void>(shareProcess);

		submitProcess(asyncProcess);
		return asyncProcess;
	}

	@Override
	public AsyncComponent<List<FileTaste>> getFileList() throws NoSessionException {
		IProcessComponent<List<FileTaste>> fileListProcess = ProcessFactory.instance().createFileListProcess(networkManager);
		AsyncComponent<List<FileTaste>> asyncProcess = new AsyncComponent<List<FileTaste>>(fileListProcess);

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