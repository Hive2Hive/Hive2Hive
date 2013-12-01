package org.hive2hive.core.process.upload.newversion;

import java.security.PublicKey;
import java.util.Set;

import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.notify.NotifyPeersProcess;
import org.hive2hive.core.process.upload.UploadFileProcessContext;

public class SendNotificationStep extends ProcessStep {

	private PublicKey modifiedFileKey;

	public SendNotificationStep(PublicKey modifiedFileKey) {
		this.modifiedFileKey = modifiedFileKey;
	}

	@Override
	public void start() {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();

		Set<String> userList = metaFolder.getUserList();
		NotifyPeersProcess notifyProcess = new NotifyPeersProcess(getNetworkManager(), userList,
				new ModifyNotifyMessageFactory(modifiedFileKey));
		notifyProcess.start();
	}

	@Override
	public void rollBack() {
		// don't do anything
		getProcess().nextRollBackStep();
	}

}
