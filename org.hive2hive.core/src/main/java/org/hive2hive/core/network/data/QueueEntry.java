package org.hive2hive.core.network.data;

import java.util.concurrent.CountDownLatch;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.versioned.UserProfile;

class QueueEntry {

	private final CountDownLatch getWaiter = new CountDownLatch(1);

	// got from DHT
	private UserProfile userProfile;
	private GetFailedException getFailedException;

	public void notifyGet() {
		getWaiter.countDown();
	}

	public void waitForGet() throws GetFailedException {
		if (getFailedException != null) {
			throw getFailedException;
		}

		try {
			getWaiter.await();
		} catch (InterruptedException e) {
			getFailedException = new GetFailedException("Could not wait for getting the user profile.");
		}

		if (getFailedException != null) {
			throw getFailedException;
		}
	}

	public void setGetError(GetFailedException error) {
		this.getFailedException = error;
	}

	public GetFailedException getGetError() {
		return getFailedException;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}
}