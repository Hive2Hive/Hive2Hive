package org.hive2hive.core.model;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The queue stores all {@link UserMessage}s that need to be processed by a client when he comes online. This
 * way, clients can communicate among each other even if they are not always online.
 * 
 * @author Nico
 * 
 */
public class UserMessageQueue {

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
}
