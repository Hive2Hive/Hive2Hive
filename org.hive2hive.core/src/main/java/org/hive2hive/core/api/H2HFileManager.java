package org.hive2hive.core.api;

import java.io.File;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Default implementation of {@link IFileManager}.
 * 
 * @author Christian, Nico
 * 
 */
public class H2HFileManager extends H2HManager implements IFileManager {

	private final IFileConfiguration fileConfiguration;

	public H2HFileManager(NetworkManager networkManager, IFileConfiguration fileConfiguration) {
		super(networkManager);
		this.fileConfiguration = fileConfiguration;
	}

	@Override
	public IProcessComponent<Void> createAddProcess(File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException {

		H2HSession session = networkManager.getSession();
		if (file == null) {
			throw new IllegalArgumentException("File cannot be null.");
		} else if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist.");
		} else if (session.getRootFile().equals(file)) {
			throw new IllegalArgumentException("Root cannot be added.");
		} else if (!FileUtil.isInH2HDirectory(session.getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not within the root file tree.");
		}

		return ProcessFactory.instance().createAddFileProcess(file, networkManager, fileConfiguration);
	}

	@Override
	public IProcessComponent<Void> createDeleteProcess(File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException {

		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		} else if (!FileUtil.isInH2HDirectory(networkManager.getSession().getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		} else if (file.isDirectory() && file.list().length > 0) {
			throw new IllegalArgumentException("Folder to delete is not empty");
		}

		return ProcessFactory.instance().createDeleteFileProcess(file, networkManager);
	}

	@Override
	public IProcessComponent<Void> createUpdateProcess(File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException {

		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		} else if (file.isDirectory()) {
			throw new IllegalArgumentException("A folder can have one version only");
		} else if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist");
		} else if (!FileUtil.isInH2HDirectory(networkManager.getSession().getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		}

		return ProcessFactory.instance().createUpdateFileProcess(file, networkManager, fileConfiguration);
	}

	@Override
	public IProcessComponent<Void> createDownloadProcess(File file) throws NoPeerConnectionException, NoSessionException,
			IllegalArgumentException {

		if (file == null) {
			throw new IllegalArgumentException("File cannot be null");
		} else if (!FileUtil.isInH2HDirectory(networkManager.getSession().getFileAgent(), file)) {
			throw new IllegalArgumentException("File is not in the Hive2Hive directory");
		}

		return ProcessFactory.instance().createDownloadFileProcess(file, networkManager);
	}

	@Override
	public IProcessComponent<Void> createMoveProcess(File source, File destination) throws NoSessionException,
			NoPeerConnectionException, IllegalArgumentException {

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

		return ProcessFactory.instance().createMoveFileProcess(source, destination, networkManager);
	}

	@Override
	public IProcessComponent<Void> createRecoverProcess(File file, IVersionSelector versionSelector)
			throws NoPeerConnectionException, NoSessionException, IllegalArgumentException {

		if (file == null) {
			throw new IllegalArgumentException("File to recover cannot be null");
		} else if (file.isDirectory()) {
			throw new IllegalArgumentException("A folder has only one version");
		}

		return ProcessFactory.instance().createRecoverFileProcess(file, versionSelector, networkManager);
	}

	@Override
	public IProcessComponent<Void> createShareProcess(File folder, String userId, PermissionType permission)
			throws NoPeerConnectionException, NoSessionException, IllegalArgumentException {

		if (folder == null) {
			throw new IllegalArgumentException("Folder to share cannot be null");
		} else if (!folder.isDirectory()) {
			throw new IllegalArgumentException("File has to be a folder.");
		} else if (!folder.exists()) {
			throw new IllegalArgumentException("Folder does not exist.");
		}

		H2HSession session = networkManager.getSession();

		// folder must be in the given root directory
		if (!FileUtil.isInH2HDirectory(session.getFileAgent(), folder)) {
			throw new IllegalArgumentException("Folder must be in root of the H2H directory.");
		}

		// sharing root folder is not allowed
		if (folder.equals(session.getRootFile())) {
			throw new IllegalArgumentException("Root folder of the H2H directory can't be shared.");
		}

		return ProcessFactory.instance().createShareProcess(folder, new UserPermission(userId, permission), networkManager);
	}

	@Override
	public IProcessComponent<FileNode> createFileListProcess() throws NoPeerConnectionException, NoSessionException {
		return ProcessFactory.instance().createFileListProcess(networkManager);
	}

	@Override
	public void subscribeFileEvents(IFileEventListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("The argument listener must not be null.");
		}
		if (networkManager.getEventBus() == null) {
			throw new IllegalStateException("No EventBus instance provided.");
		}
		networkManager.getEventBus().subscribe(listener);
	}
}
