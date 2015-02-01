package org.hive2hive.core.processes.common.base;

import org.hive2hive.core.exceptions.AbortModifyException;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.data.IUserProfileModification;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can be extended by process steps that want to modify the user profile
 * 
 * @author Nico
 *
 */
public abstract class BaseModifyUserProfileStep extends ProcessStep<Void> implements IUserProfileModification {

	private static final Logger logger = LoggerFactory.getLogger(BaseModifyUserProfileStep.class);
	private final UserProfileManager profileManager;

	public BaseModifyUserProfileStep(UserProfileManager profileManager) {
		this.profileManager = profileManager;
	}

	@Override
	protected final Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		beforeModify();
		try {
			profileManager.modifyUserProfile(getID(), this);
			setRequiresRollback(true);
		} catch (GetFailedException | PutFailedException | AbortModifyException e) {
			logger.error("Cannot modify the user profile", e);
			throw new ProcessExecutionException(this, e);
		}
		afterModify();
		return null;
	}

	@Override
	protected final Void doRollback() throws InvalidProcessStateException, ProcessRollbackException {
		// only do this if the modification of the UP was successful
		try {
			profileManager.modifyUserProfile(getID(), new RollbackUPModification());
		} catch (GetFailedException | PutFailedException | AbortModifyException e) {
			logger.error("Cannot modify the user profile", e);
			throw new ProcessRollbackException(this, e);
		}
		return null;
	}

	/**
	 * Is called <strong>before</strong> {@link IUserProfileModification#modifyUserProfile(UserProfile)}.
	 * However, this method is only called once, thus ideal for slow operations.
	 */
	protected void beforeModify() throws ProcessExecutionException {
		// optional to overwrite
	}

	/**
	 * Is called <strong>after</strong> {@link IUserProfileModification#modifyUserProfile(UserProfile)}. It's
	 * good for preparation of next process steps, cleanups or other things that should be done only once.
	 */
	protected void afterModify() throws ProcessExecutionException {
		// optional to overwrite
	}

	/**
	 * Is called to un-modify the user profile during rollback. During rollback, {@link #beforeModify()} and
	 * {@link #afterModify()} are not called anymore.
	 */
	protected abstract void modifyRollback(UserProfile userProfile);

	private class RollbackUPModification implements IUserProfileModification {

		@Override
		public void modifyUserProfile(UserProfile userProfile) {
			modifyRollback(userProfile);
		}

	}
}
