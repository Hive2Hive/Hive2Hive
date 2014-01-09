package org.hive2hive.core.process.userprofiletask;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.userprofiletask.GetUserProfileTaskStep;

/**
 * A process which gets one by one an encrypted {@link UserProfileTask} object from the user profile task
 * queue which is located on the proxy node of logged in user. The process works as follows:
 * <ul>
 * <li>1. Get oldest user profile task from the queue (for details about the queue see {@link UserProfileTask}
 * . Decrypt the received task. If no one user profile task is available the process finishes.</li>
 * <li>2. Execute the fetched task and wait till it finished.</li>
 * <li>3. Remove the received user profile task from the network.</li>
 * <li>4. got to step 1.</li>
 * </ul>
 * 
 * @author Seppi
 */
public class UserProfileTaskQueueProcess extends Process {

	private final static Logger logger = H2HLoggerFactory.getLogger(UserProfileTaskQueueProcess.class);

	private final UserProfileTaskQueueProcessContext context;

	public UserProfileTaskQueueProcess(NetworkManager networkManager) throws NoSessionException {
		super(networkManager);

		H2HSession session = networkManager.getSession();

		context = new UserProfileTaskQueueProcessContext(this, session.getProfileManager(),
				session.getFileManager());

		logger.debug("Handling user profile task queue.");

		HandleUserProfileTaskStep handleUserProfileTaskStep = new HandleUserProfileTaskStep(context);
		GetUserProfileTaskStep getUserProfileTaskStep = new GetUserProfileTaskStep(context, handleUserProfileTaskStep);
		setNextStep(getUserProfileTaskStep);
	}

	@Override
	public UserProfileTaskQueueProcessContext getContext() {
		return context;
	}

}
