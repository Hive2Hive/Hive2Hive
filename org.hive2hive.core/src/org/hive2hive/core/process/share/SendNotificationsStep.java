package org.hive2hive.core.process.share;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.notify.BaseNotificationMessageFactory;

/**
 * Sends a notification message to an user to check his user profile task queue.
 * 
 * @author Seppi, Nico
 */
public class SendNotificationsStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(SendNotificationsStep.class);

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();

		// notify all users that share this folder (already shared or newly shared)
		MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();
		Set<String> otherUsers = new HashSet<String>(metaFolder.getUserList());
		otherUsers.remove(context.getSession().getCredentials().getUserId());
		logger.debug(String
				.format("Sending a notification message to %s# other sharing user(s) about a newly added sharing user.",
						otherUsers.size()));
		BaseNotificationMessageFactory messageFactory = new ShareFolderNotificationMessageFactory(
				metaFolder.getId(), context.getProtectionKeys(), context.getFriendId());
		getProcess().sendNotification(messageFactory, otherUsers);
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}

}
