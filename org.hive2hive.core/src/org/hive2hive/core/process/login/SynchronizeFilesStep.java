package org.hive2hive.core.process.login;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.upload.UploadFileProcess;

public class SynchronizeFilesStep extends ProcessStep {

	@Override
	public void start() {
		PostLoginProcessContext context = (PostLoginProcessContext) getProcess().getContext();
		FileManager fileManager = context.getFileManager();
		UserProfile userProfile = context.getUserProfile();

		// synchronize the files that need to be downloaded from the DHT. Since the missing files are returned
		// in preorder, we can easily build a tree from the list. Each child waits for execution until the
		// parent is executed.
		List<FileTreeNode> missingOnDisk = fileManager.getMissingOnDisk(userProfile.getRoot());
		ProcessTreeNode rootProcess = new ProcessTreeNode(null, null, null);
		for (FileTreeNode missing : missingOnDisk) {
			ProcessTreeNode parent = getParent(rootProcess, missing);
			// initialize the process
			DownloadFileProcess downloadProcess = new DownloadFileProcess(missing, getNetworkManager(),
					fileManager);
			new ProcessTreeNode(downloadProcess, parent, missing);
		}

		rootProcess.start();

		// synchronize the files that need to be uploaded into the DHT
		Set<File> missingInTree = fileManager.getMissingInTree(userProfile.getRoot());
		for (File file : missingInTree) {
			UploadFileProcess process = new UploadFileProcess(file, context.getCredentials(),
					getNetworkManager(), fileManager, context.getFileConfig());
			process.start();
		}

		// TODO how detect files that have been deleted locally while offline?

		// TODO wait for all processes to have finished (failed or not failed) until continuing with next
		// step (user messages).
	}

	@Override
	public void rollBack() {
		// TODO Auto-generated method stub
	}

	private ProcessTreeNode getParent(ProcessTreeNode root, FileTreeNode node) {
		ProcessTreeNode current = root;
		for (ProcessTreeNode child : root.getChildren()) {
			FileTreeNode childNode = child.getNode();
			if (!childNode.isFolder()) {
				// can skip files because they cannot be parents
				continue;
			} else {
				if (node.getFullPath().startsWith(childNode.getFullPath())) {
					current = child;
				}
			}
		}
		return current;
	}

	private class ProcessTreeNode extends Process {

		private final Process process;
		private final List<ProcessTreeNode> childProcesses;
		private final FileTreeNode node;

		public ProcessTreeNode(Process process, ProcessTreeNode parent, FileTreeNode node) {
			super(null);
			this.process = process;
			this.node = node;
			this.childProcesses = new ArrayList<ProcessTreeNode>();
			if (parent != null) {
				parent.addChild(this);
			}
		}

		public void addChild(ProcessTreeNode childProcess) {
			childProcesses.add(childProcess);
		}

		public List<ProcessTreeNode> getChildren() {
			return childProcesses;
		}

		public FileTreeNode getNode() {
			return node;
		}

		@Override
		public void start() {
			if (process == null) {
				// is root node --> start all children
				for (ProcessTreeNode child : childProcesses) {
					child.start();
				}
			} else {
				// after the current process is done, start the next process
				for (final ProcessTreeNode child : childProcesses) {
					process.addListener(new IProcessListener() {
						@Override
						public void onSuccess() {
							// start the child
							child.start();
						}

						@Override
						public void onFail(String reason) {
							// do not start the child processes
						}
					});
				}

				process.start();
			}
		}
	}

}
