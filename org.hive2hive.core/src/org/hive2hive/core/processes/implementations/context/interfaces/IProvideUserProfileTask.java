package org.hive2hive.core.processes.implementations.context.interfaces;

import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.processes.implementations.common.userprofiletask.GetUserProfileTaskStep;

/**
 * Simple interface which allows a context to set an user profile task through the
 * {@link GetUserProfileTaskStep} process step.
 * 
 * @author Seppi, Nico
 */
public interface IProvideUserProfileTask {

	void provideUserProfileTask(UserProfileTask profileTask);
}
