package org.hive2hive.core.process.share.notify;

import java.security.PublicKey;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;

public class ShareFolderNotificationProcess extends Process {
	
	private final ShareFolderNotificationProcessContext context;

	public ShareFolderNotificationProcess(PublicKey folderKey, NetworkManager networkManager) {
		super(networkManager);
		
		context = new ShareFolderNotificationProcessContext(this, folderKey);

		setNextStep(null);
	}

	@Override
	public ShareFolderNotificationProcessContext getContext() {
		return context;
	}

}
