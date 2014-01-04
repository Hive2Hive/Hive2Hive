package org.hive2hive.core.process.share;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.notify.INotificationMessageFactory;

public class SendNotificationStep extends ProcessStep {

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();
		MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();
		KeyPair domainKey = context.getDomainKey();

		INotificationMessageFactory factory = new ShareFolderNotificationMessageFactory(metaFolder.getId(), domainKey);
		
		getProcess().notifyOtherClients(factory);
		getProcess().notfyOtherUsers(metaFolder.getUserList(), factory);
		getProcess().setNextStep(null);
	}

	@Override
	public void rollBack() {
		// don't do anything
		getProcess().nextRollBackStep();
	}

}
