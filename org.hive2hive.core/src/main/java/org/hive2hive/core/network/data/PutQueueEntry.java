package org.hive2hive.core.network.data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hive2hive.core.exceptions.PutFailedException;

class PutQueueEntry extends QueueEntry {

	private final AtomicBoolean readyToPut = new AtomicBoolean(false);
	private final AtomicBoolean abort = new AtomicBoolean(false);
	private final CountDownLatch putWaiter = new CountDownLatch(1);

	private PutFailedException putFailedException;

	public PutQueueEntry(String pid) {
		super(pid);
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
			putWaiter.await();
		} catch (InterruptedException e) {
			putFailedException = new PutFailedException("Could not wait to put the user profile");
		}

		if (putFailedException != null) {
			throw putFailedException;
		}
	}

	public void setPutError(PutFailedException error) {
		this.putFailedException = error;
	}
}