package org.hive2hive.core.processes.context;

import java.io.File;
import java.util.Set;

import org.hive2hive.core.processes.context.interfaces.INotifyContext;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

/**
 * @author Nico, Seppi
 */
public class MoveFileProcessContext {

	private final File source;
	private final File destination;
	private final File root;

	// three context objects because we need to send three different types of notifications:
	// 1. users that had access before and after move
	// 2. users that don't have access anymore
	// 3. users that newly have access
	private final MoveNotificationContext moveNotificationContext;
	private final DeleteNotificationContext deleteNotificationContext;
	private final AddNotificationContext addNotificationContext;

	public MoveFileProcessContext(File source, File destination, File root) {
		this.source = source;
		this.destination = destination;
		this.root = root;

		moveNotificationContext = new MoveNotificationContext();
		deleteNotificationContext = new DeleteNotificationContext();
		addNotificationContext = new AddNotificationContext();
	}

	public File getSource() {
		return source;
	}

	public File getDestination() {
		return destination;
	}

	public File getRoot() {
		return root;
	}

	public MoveNotificationContext getMoveNotificationContext() {
		return moveNotificationContext;
	}

	public DeleteNotificationContext getDeleteNotificationContext() {
		return deleteNotificationContext;
	}

	public AddNotificationContext getAddNotificationContext() {
		return addNotificationContext;
	}

	/**
	 * for users having before and after access to the file
	 */
	public class MoveNotificationContext implements INotifyContext {
		private BaseNotificationMessageFactory messageFactory;
		private Set<String> users;

		@Override
		public BaseNotificationMessageFactory consumeMessageFactory() {
			return messageFactory;
		}

		@Override
		public Set<String> consumeUsersToNotify() {
			return users;
		}

		public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
			this.messageFactory = messageFactory;
		}

		public void provideUsersToNotify(Set<String> users) {
			this.users = users;
		}
	}

	/**
	 * for users not having access anymore
	 */
	public class DeleteNotificationContext implements INotifyContext {
		private BaseNotificationMessageFactory messageFactory;
		private Set<String> users;

		@Override
		public BaseNotificationMessageFactory consumeMessageFactory() {
			return messageFactory;
		}

		@Override
		public Set<String> consumeUsersToNotify() {
			return users;
		}

		public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
			this.messageFactory = messageFactory;
		}

		public void provideUsersToNotify(Set<String> users) {
			this.users = users;
		}
	}

	/**
	 * for users now having access
	 */
	public class AddNotificationContext implements INotifyContext {
		private BaseNotificationMessageFactory messageFactory;
		private Set<String> users;

		@Override
		public BaseNotificationMessageFactory consumeMessageFactory() {
			return messageFactory;
		}

		@Override
		public Set<String> consumeUsersToNotify() {
			return users;
		}

		public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
			this.messageFactory = messageFactory;
		}

		public void provideUsersToNotify(Set<String> users) {
			this.users = users;
		}
	}

}
