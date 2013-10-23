package org.hive2hive.core.model;

import java.util.LinkedList;
import java.util.Queue;

import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.network.data.DataWrapper;

/**
 * The queue stores all {@link UserMessage}s that need to be processed by a client when he comes online. This
 * way, clients can communicate among each other even if they are not always online.
 * 
 * @author Nico
 * 
 */
public class UserMessageQueue extends DataWrapper {

	private static final long serialVersionUID = 1L;
	private final String forUser;
	private final Queue<UserMessage> messageQueue;

	public UserMessageQueue(String forUser) {
		this.forUser = forUser;
		messageQueue = new LinkedList<UserMessage>();
	}

	public String getForUser() {
		return forUser;
	}

	public Queue<UserMessage> getMessageQueue() {
		return messageQueue;
	}

	@Override
	public int getTimeToLive() {
		return TimeToLiveStore.getInstance().getUserMessageQueue();
	}
}
