package org.hive2hive.core.process.upload.newversion;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.upload.UploadFileProcessContext;
import org.hive2hive.core.process.upload.UploadNotificationMessageFactory;

public class SendNotificationStep extends ProcessStep {

	private PublicKey modifiedFileKey;

	public SendNotificationStep(PublicKey modifiedFileKey) {
		this.modifiedFileKey = modifiedFileKey;
	}

	@Override
	public void start() {
		UploadFileProcessContext context = (UploadFileProcessContext) getProcess().getContext();
		MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();

		Set<String> userList = new HashSet<String>();
		if (metaFolder != null) {
			userList.addAll(metaFolder.getUserList());
		}

		getProcess().sendNotification(new UploadNotificationMessageFactory(modifiedFileKey, userList));
		getProcess().setNextStep(null);
	}

	@Override
	public void rollBack() {
		// don't do anything
		getProcess().nextRollBackStep();
	}

}
