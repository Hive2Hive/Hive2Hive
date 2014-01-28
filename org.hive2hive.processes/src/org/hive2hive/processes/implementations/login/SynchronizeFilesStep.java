package org.hive2hive.processes.implementations.login;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;
import org.hive2hive.processes.implementations.files.util.FileRecursionUtil;

public class SynchronizeFilesStep extends ProcessStep {

	private final NetworkManager networkManager;

	public SynchronizeFilesStep(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		UserProfileManager profileManager;
		FileManager fileManager;
		try {
			profileManager = networkManager.getSession().getProfileManager();
			fileManager = networkManager.getSession().getFileManager();
		} catch (NoSessionException e) {
			cancel(new RollbackReason(this, "No session"));
			return;
		}

		UserProfile profile = null;
		try {
			profile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			cancel(new RollbackReason(this, "User profile could not be accessed."));
			return;
		}

		FileSynchronizer synchronizer = new FileSynchronizer(fileManager, profile);

		/*
		 * count up to 4:
		 * - until the uploadProcessNewFiles is done
		 * - until the uploadProcessNewVersions is done
		 * - until the deleteProess is done
		 * - until the downloadProcess is done
		 */
		CountDownLatch latch = new CountDownLatch(4);
		CountDownListener latchListener = new CountDownListener(latch);

		// download remotely added/updated files
		List<FileTreeNode> toDownload = new ArrayList<FileTreeNode>(synchronizer.getAddedRemotely());
		toDownload.addAll(synchronizer.getUpdatedRemotely());
		ProcessComponent downloadProcess = FileRecursionUtil.buildDownloadProcess(toDownload, networkManager);
		downloadProcess.attachListener(latchListener);

		// TODO: if is master, add the "ProcessFactory.instance().createUserProfileTaskStep(networkManager);"
	}

	private class CountDownListener implements IProcessComponentListener {

		private final CountDownLatch latch;

		public CountDownListener(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void onSucceeded() {
			latch.countDown();
		}

		@Override
		public void onFailed() {
			latch.countDown();
		}

		@Override
		public void onFinished() {
			// ignore
		}

	}

}
