package org.hive2hive.core.process.upload;

import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.download.DownloadFileProcess;

@Deprecated
// does not work for shared files
public class UploadUserProfileTask extends UserProfileTask {

	private static final long serialVersionUID = -4568985873058024202L;
	private final static Logger logger = H2HLoggerFactory.getLogger(UploadUserProfileTask.class);
	private final PublicKey fileKey;

	public UploadUserProfileTask(PublicKey fileKey) {
		this.fileKey = fileKey;
	}

	@Override
	public void start() {
		try {
			DownloadFileProcess process = new DownloadFileProcess(fileKey, networkManager);
			process.start();
			logger.debug("Starte downloading a file");
		} catch (GetFailedException | NoSessionException | IllegalArgumentException e) {
			logger.error("Could not download the file", e);
			return;
		}

		notifyOtherClients(new UploadNotificationMessageFactory(fileKey));
		logger.debug("Notified other clients that a file has been update by another user");
	}
}
