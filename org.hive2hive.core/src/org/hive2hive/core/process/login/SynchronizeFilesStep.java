package org.hive2hive.core.process.login;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hive2hive.core.IH2HFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.ProcessTreeNode;
import org.hive2hive.core.process.delete.DeleteFileProcess;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.upload.newfile.NewFileProcess;
import org.hive2hive.core.process.upload.newversion.NewVersionProcess;

/**
 * Synchronizes the local files with the entries in the user profile:
 * <ul>
 * <li>Files that have been added to the user profile while the client was offline --> missing on disk</li>
 * <li>Files that have been added to the folder on disk while the client was offline --> missing in
 * userprofile</li>
 * <li>Files that have been changed during the client was offline. The changes could have been made in the
 * userprofile or on the local disc. If changes on both locations have been made, the version in the user
 * profile wins.</li>
 * <li>If a file was deleted on disk during offline phase, the file gets deleted in the DHT too.</li>
 * <li>If a file was deleted in the user profile, the file gets deleted on disk too.</li>
 * </ul>
 * 
 * @author Nico
 * 
 */
public class SynchronizeFilesStep extends ProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(SynchronizeFilesStep.class);

	// collects the problems of the concurrent processes executed in this step
	List<String> problems = new CopyOnWriteArrayList<String>();

	@Override
	public void start() {
		PostLoginProcessContext context = (PostLoginProcessContext) getProcess().getContext();
		FileManager fileManager = context.getFileManager();
		UserProfileManager profileManager = context.getProfileManager();

		FileSynchronizer synchronizer = null;
		try {
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), false);
			synchronizer = new FileSynchronizer(fileManager, userProfile);
		} catch (GetFailedException e1) {
			getProcess().stop(e1.getMessage());
			return;
		}

		/*
		 * Download the remotely added and updated files
		 */
		List<FileTreeNode> toDownload = synchronizer.getAddedRemotely();
		toDownload.addAll(synchronizer.getUpdatedRemotely());
		ProcessTreeNode downloadProcess = startDownload(toDownload, fileManager);

		/*
		 * Upload the locally added and updated files
		 */
		List<File> toUploadNewFiles = synchronizer.getAddedLocally();
		ProcessTreeNode uploadProcessNewFiles = startUploadNewFiles(toUploadNewFiles, fileManager,
				profileManager, context.getFileConfig());
		List<File> toUploadNewVersions = synchronizer.getUpdatedLocally();
		ProcessTreeNode uploadProcessNewVersions = startUploadNewVersion(toUploadNewVersions, fileManager,
				profileManager, context.getFileConfig());

		/*
		 * Delete the files in the DHT
		 */
		List<FileTreeNode> toDeleteInDHT = synchronizer.getDeletedLocally();
		ProcessTreeNode deleteProcess = startDelete(toDeleteInDHT, fileManager, profileManager);

		/*
		 * Delete the remotely deleted files
		 */
		List<File> toDeleteOnDisk = synchronizer.getDeletedRemotely();
		for (File file : toDeleteOnDisk) {
			file.delete();
		}

		while (!(downloadProcess.isDone() && uploadProcessNewFiles.isDone()
				&& uploadProcessNewVersions.isDone() && deleteProcess.isDone())) {
			try {
				logger.debug("Waiting until uploads and downloads finish...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		logger.debug("All uploads / downloads are done");
		for (String problem : problems) {
			logger.error("Problem occurred: " + problem);
		}

		if (context.getIsDefinedAsMaster()) {
			// TODO set when step is implemented
			// String userId = profileManager.getUserCredentials().getUserId();
			// HandleUserMessageQueueStep handleUmQueueStep = new HandleUserMessageQueueStep(userId);
			// GetUserMessageQueueStep getUMQueueStep = new GetUserMessageQueueStep(userId,
			// handleUmQueueStep);
			// context.setUserMessageQueueStep(getUMQueueStep);
			// getProcess().setNextStep(getUMQueueStep);
			getProcess().setNextStep(null);
		} else {
			// done with the post login process
			getProcess().setNextStep(null);
		}
	}

	/**
	 * Does not affect other processes because it only touches the files to download
	 */
	private ProcessTreeNode startDownload(List<FileTreeNode> toDownload, FileManager fileManager) {
		// synchronize the files that need to be downloaded from the DHT. Since the missing files are returned
		// in preorder, we can easily build a tree from the list. Each child waits for execution until the
		// parent is executed.
		NodeProcessTreeNode rootProcess = new NodeProcessTreeNode();
		for (FileTreeNode node : toDownload) {
			ProcessTreeNode parent = getParent(rootProcess, node);
			// initialize the process
			DownloadFileProcess downloadProcess = new DownloadFileProcess(node, getNetworkManager(),
					fileManager);
			new NodeProcessTreeNode(downloadProcess, parent, node);
		}

		// start the download
		if (toDownload.size() > 0)
			logger.debug("Start downloading new and modified files...");

		rootProcess.start();
		return rootProcess;
	}

	private ProcessTreeNode startUploadNewFiles(List<File> toUpload, FileManager fileManager,
			UserProfileManager profileManager, IH2HFileConfiguration config) {
		// synchronize the files that need to be uploaded into the DHT
		FileProcessTreeNode rootProcess = new FileProcessTreeNode();
		for (File file : toUpload) {
			ProcessTreeNode parent = getParent(rootProcess, file);
			try {
				// initialize the process
				NewFileProcess uploadProcess = new NewFileProcess(file, profileManager, getNetworkManager(),
						fileManager, config);
				new FileProcessTreeNode(uploadProcess, parent, file);
			} catch (IllegalFileLocation e) {
				logger.error("File cannot be uploaded", e);
			}
		}

		if (toUpload.size() > 0)
			logger.debug("Start uploading new files ");

		rootProcess.start();
		return rootProcess;
	}

	private ProcessTreeNode startUploadNewVersion(List<File> toUpload, FileManager fileManager,
			UserProfileManager profileManager, IH2HFileConfiguration config) {
		// synchronize the files that need to be uploaded into the DHT
		FileProcessTreeNode rootProcess = new FileProcessTreeNode();
		for (File file : toUpload) {
			ProcessTreeNode parent = getParent(rootProcess, file);
			// initialize the process
			NewVersionProcess uploadProcess = new NewVersionProcess(file, profileManager,
					getNetworkManager(), fileManager, config);
			new FileProcessTreeNode(uploadProcess, parent, file);
		}

		if (toUpload.size() > 0)
			logger.debug("Start uploading new versions of files ");

		rootProcess.start();
		return rootProcess;
	}

	private ProcessTreeNode startDelete(List<FileTreeNode> toDelete, FileManager fileManager,
			UserProfileManager profileManager) {
		// delete the files in the DHT that are deleted while offline. First create a normal tree, but the
		// order must be reverse. With the created tree, reverse it in a 2nd step.
		List<ProcessTreeNode> allNodes = new ArrayList<ProcessTreeNode>();
		NodeProcessTreeNode rootProcess = new NodeProcessTreeNode();
		for (FileTreeNode node : toDelete) {
			ProcessTreeNode parent = getParent(rootProcess, node);
			// initialize the process
			DeleteFileProcess deleteProcess = new DeleteFileProcess(node, profileManager, fileManager,
					getNetworkManager());
			allNodes.add(new NodeProcessTreeNode(deleteProcess, parent, node));
		}

		// remove the rootProcess from the process manager because it will never be started
		ProcessManager.getInstance().detachProcess(rootProcess);

		// files are deleted in reverse order (not pre-order)
		// get parent means get the child files --> start deletion at children,
		// thus the tree must be reversed (by using the depth)
		NodeProcessTreeNode reverseRootProcess = new NodeProcessTreeNode();
		ProcessTreeNode currentParent = reverseRootProcess;
		int currentDepth = allNodes.size();
		while (currentDepth >= 0) {
			for (ProcessTreeNode processNode : allNodes) {
				if (processNode.getDepth() == currentDepth) {
					currentParent.addChild(processNode);
				}
			}

			if (currentParent.getChildren() != null && !currentParent.getChildren().isEmpty()) {
				// children are filled --> take first child as new parent
				currentParent = currentParent.getChildren().get(0);
			}

			// decrease depth
			currentDepth--;
		}

		// start the download
		if (toDelete.size() > 0)
			logger.debug("Start deleting files in DHT...");

		reverseRootProcess.start();
		return reverseRootProcess;
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}

	private ProcessTreeNode getParent(NodeProcessTreeNode root, FileTreeNode node) {
		ProcessTreeNode current = root;
		for (ProcessTreeNode child : root.getChildren()) {
			NodeProcessTreeNode childProcess = (NodeProcessTreeNode) child;
			FileTreeNode childNode = childProcess.getNode();
			if (childNode.isFolder()) {
				// skip non-directories
				if (node.getFullPath().startsWith(childNode.getFullPath())) {
					current = child;
				}
			}
		}
		return current;
	}

	private ProcessTreeNode getParent(FileProcessTreeNode root, File file) {
		ProcessTreeNode current = root;
		for (ProcessTreeNode child : root.getChildren()) {
			FileProcessTreeNode childProcess = (FileProcessTreeNode) child;
			File childNode = childProcess.getFile();
			if (childNode.isDirectory()) {
				// skip non-directories
				if (file.getAbsolutePath().startsWith(childNode.getAbsolutePath())) {
					current = child;
				}
			}
		}
		return current;
	}

	/**
	 * Additionally holds a {@link FileTreeNode}
	 * 
	 * @author Nico
	 * 
	 */
	private class NodeProcessTreeNode extends ProcessTreeNode {

		private final FileTreeNode node;

		public NodeProcessTreeNode(Process process, ProcessTreeNode parent, FileTreeNode node) {
			super(process, parent);
			this.node = node;
		}

		public NodeProcessTreeNode() {
			super();
			this.node = null;
		}

		public FileTreeNode getNode() {
			return node;
		}

		@Override
		public void onFail(String reason) {
			problems.add(reason);
		}
	}

	/**
	 * Additionally holds a {@link File}
	 * 
	 * @author Nico
	 * 
	 */
	private class FileProcessTreeNode extends ProcessTreeNode {
		private final File file;

		public FileProcessTreeNode(Process process, ProcessTreeNode parent, File file) {
			super(process, parent);
			this.file = file;
		}

		public FileProcessTreeNode() {
			super();
			this.file = null;
		}

		public File getFile() {
			return file;
		}

		@Override
		public void onFail(String reason) {
			problems.add(reason);
		}
	}

}
