package org.hive2hive.core.network.data;

import java.util.concurrent.CountDownLatch;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.model.UserProfile;

class QueueEntry {

	private final String pid;

	private final CountDownLatch getWaiter = new CountDownLatch(1);

	// got from DHT
	private UserProfile userProfile;
	private GetFailedException getFailedException;

	public QueueEntry(String pid) {
		this.pid = pid;
	}

	public String getPid() {
		return pid;
	}

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

	@Override
	public int hashCode() {
		return pid.hashCode();
	}

	@Override
	public boolean equals(Object otherPid) {
		if (otherPid instanceof String) {
			String pidString = (String) otherPid;
			return pid.equals(pidString);
		} else if (otherPid instanceof QueueEntry) {
			QueueEntry otherEntry = (QueueEntry) otherPid;
			return pid.equals(otherEntry.getPid());
		}
		return false;
	}
}