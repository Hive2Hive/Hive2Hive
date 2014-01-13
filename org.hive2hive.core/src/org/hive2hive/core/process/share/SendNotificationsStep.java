package org.hive2hive.core.process.share;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.userprofiletask.UserProfileTaskNotificationMessageFactory;
import org.hive2hive.core.process.common.userprofiletask.UserProfileTaskNotificationMessageFactory.Type;

/**
 * Sends a notification message to an user to check his user profile task queue.
 * 
 * @author Seppi
 */
public class SendNotificationsStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(SendNotificationsStep.class);

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();

		// notify the newly added user of the shared folder
		getProcess().notifyOtherUser(context.getFriendId(),
				new UserProfileTaskNotificationMessageFactory(Type.SHARING_FOLDER));

		// notify other sharing users about the newly added user
		MetaFolder metaFolder = (MetaFolder) context.getMetaDocument();
		Set<String> otherUsers = new HashSet<String>(metaFolder.getUserList());
		otherUsers.remove(context.getFriendId());
		otherUsers.remove(context.getSession().getCredentials().getUserId());
		if (!otherUsers.isEmpty()) {
			logger.debug(String
					.format("Sending a notification message to %s# other sharing user(s) about a newly added sharing user.",
							otherUsers.size()));
			getProcess().notfyOtherUsers(null, new ShareFolderNotificationMessageFactory(metaFolder.getId(), context.getProtectionKeys()));
		}
		
		// notify other clients about the newly added user
		// TODO currently not necessary here, but maybe later
	}

	@Override
	public void rollBack() {
		getProcess().nextRollBackStep();
	}

}
