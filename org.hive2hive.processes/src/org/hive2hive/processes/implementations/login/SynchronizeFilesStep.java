package org.hive2hive.processes.implementations.login;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.file.FileSynchronizer;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.util.FileRecursionUtil;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeSession;

public class SynchronizeFilesStep extends ProcessStep {

	private final IConsumeSession context;

	public SynchronizeFilesStep(IConsumeSession context) {
		this.context = context;
	}
	
	@Override
	protected void doExecute() throws InvalidProcessStateException {

		UserProfileManager profileManager = context.consumeSession().getProfileManager();
		FileManager fileManager = context.consumeSession().getFileManager();
		
		UserProfile profile = null;
		try {
			profile = profileManager.getUserProfile(0, false);
		} catch (GetFailedException e) {
			cancel(new RollbackReason(this, "User profile could not be accessed."));
		}
		
		FileSynchronizer synchronizer = new FileSynchronizer(fileManager, profile);
		
		// wait for all processes to be done
		CountDownLatch latch = new CountDownLatch(4);
		CountDownListener latchListener = new CountDownListener(latch);
		
		// download remotely added/updated files
		List<FileTreeNode> toDownload = new ArrayList<FileTreeNode>(synchronizer.getAddedRemotely());
		toDownload.addAll(synchronizer.getUpdatedRemotely());
		
	}
	
	private void startDownload(List<FileTreeNode> toDownload) {
		
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
