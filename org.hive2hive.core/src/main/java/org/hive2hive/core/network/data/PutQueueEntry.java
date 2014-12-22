package org.hive2hive.core.network.data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;

class PutQueueEntry extends QueueEntry {

	private final String pid;
	private final AtomicBoolean readyToPut = new AtomicBoolean(false);
	private final AtomicBoolean abort = new AtomicBoolean(false);
	private final CountDownLatch putWaiter = new CountDownLatch(1);

	private PutFailedException putFailedException;

	public PutQueueEntry(String pid) {
		this.pid = pid;
	}

	public String getPid() {
		return pid;
	}

	public boolean isReadyToPut() {
		return readyToPut.get();
	}

	public void readyToPut() {
		readyToPut.set(true);
	}

	public boolean isAborted() {
		return abort.get();
	}

	public void abort() {
		abort.set(true);
	}

	public void notifyPut() {
		putWaiter.countDown();
	}

	public void waitForPut() throws PutFailedException {
		if (putFailedException != null) {
			throw putFailedException;
		}

		try {
			boolean success = putWaiter.await(H2HConstants.AWAIT_NETWORK_OPERATION_MS * H2HConstants.PUT_RETRIES,
					TimeUnit.MILLISECONDS);
			if (!success) {
				throw new PutFailedException("Timeout while putting occurred");
			}
		} catch (InterruptedException e) {
			throw new PutFailedException("Could not wait to put the user profile");
		}

		if (putFailedException != null) {
			throw putFailedException;
		}
	}

	public void setPutError(PutFailedException error) {
		this.putFailedException = error;
	}

	@Override
	public int hashCode() {
		return getPid().hashCode();
	}

	@Override
	public boolean equals(Object otherPid) {
		if (otherPid instanceof String) {
			String pidString = (String) otherPid;
			return getPid().equals(pidString);
		} else if (otherPid instanceof PutQueueEntry) {
			PutQueueEntry otherEntry = (PutQueueEntry) otherPid;
			return getPid().equals(otherEntry.getPid());
		}
		return false;
	}
}