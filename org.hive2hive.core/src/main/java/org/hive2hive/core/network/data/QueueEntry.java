package org.hive2hive.core.network.data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.versioned.UserProfile;

class QueueEntry {

	private final CountDownLatch getWaiter = new CountDownLatch(1);

	// got from DHT
	private UserProfile userProfile;
	private GetFailedException getFailedException;

	public void setGetError(GetFailedException error) {
		this.getFailedException = error;
		getWaiter.countDown();
	}

	/**
	 * Returns the user profile (blocking) as soon as it's ready
	 * 
	 * @return the user profile
	 */
	public UserProfile getUserProfile() throws GetFailedException {
		if (getFailedException != null) {
			// exception already here, don't even wait
			throw getFailedException;
		}

		try {
			boolean success = getWaiter.await(H2HConstants.AWAIT_NETWORK_OPERATION_MS, TimeUnit.MILLISECONDS);
			if (!success) {
				throw new GetFailedException("Could not wait for getting the user profile");
			}
		} catch (InterruptedException e) {
			throw new GetFailedException("Could not wait for getting the user profile.");
		}

		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
		getWaiter.countDown();
	}
}