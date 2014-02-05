package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.HybridEncryptedContent;

public class MoveFileProcessContext implements IProvideMetaDocument, IConsumeMetaDocument,
		IProvideProtectionKeys, IConsumeProtectionKeys {

	private final File source;
	private final File destination;
	private final boolean sourceInRoot;
	private final boolean destinationInRoot;

	private KeyPair protectionKeys;
	private MetaDocument metaDocument;
	private KeyPair fileNodeKeys;

	private final HashSet<String> usersToNotifySource;
	private final HashSet<String> usersToNotifyDestination;

	// three context objects because we need to send three different types of notifications:
	// 1. users that had access before and after move
	// 2. users that don't have access anymore
	// 3. users that newly have access
	private final MoveNotificationContext moveNotificationContext;
	private final DeleteNotificationContext deleteNotificationContext;
	private final AddNotificationContext addNotificationContext;

	public MoveFileProcessContext(File source, File destination, boolean sourceInRoot,
			boolean destinationInRoot, String userId) {
		this.source = source;
		this.destination = destination;
		this.sourceInRoot = sourceInRoot;
		this.destinationInRoot = destinationInRoot;
		this.usersToNotifySource = new HashSet<String>();
		usersToNotifySource.add(userId);
		this.usersToNotifyDestination = new HashSet<String>();
		usersToNotifyDestination.add(userId);

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

	public boolean isSourceInRoot() {
		return sourceInRoot;
	}

	public boolean isDestinationInRoot() {
		return destinationInRoot;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;

	}

	@Override
	public MetaDocument consumeMetaDocument() {
		return metaDocument;
	}

	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public void provideEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}

	public void setFileNodeKeys(KeyPair fileNodeKeys) {
		this.fileNodeKeys = fileNodeKeys;
	}

	public KeyPair getFileNodeKeys() {
		return fileNodeKeys;
	}

	/**
	 * Users that have access to the source folder (can overlap with destinations folder)s
	 */
	public void addUsersToNotifySource(Set<String> userIds) {
		usersToNotifySource.addAll(userIds);
	}

	/**
	 * Users to be notified that the file has been removed from the source folder
	 */
	public Set<String> getUsersToNotifySource() {
		return usersToNotifySource;
	}

	/**
	 * Users that have access to the destination folder (can overlap with source folder)
	 */
	public void addUsersToNotifyDestination(Set<String> userIds) {
		usersToNotifyDestination.addAll(userIds);
	}

	/**
	 * Users to be notified that the file has been added to the destination folder
	 */
	public Set<String> getUsersToNotifyDestination() {
		return usersToNotifyDestination;
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
	public class MoveNotificationContext implements IConsumeNotificationFactory, IProvideNotificationFactory {
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

		@Override
		public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
			this.messageFactory = messageFactory;
		}

		@Override
		public void provideUsersToNotify(Set<String> users) {
			this.users = users;
		}
	}

	/**
	 * for users not having access anymore
	 */
	public class DeleteNotificationContext implements IConsumeNotificationFactory,
			IProvideNotificationFactory {
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

		@Override
		public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
			this.messageFactory = messageFactory;
		}

		@Override
		public void provideUsersToNotify(Set<String> users) {
			this.users = users;
		}
	}

	/**
	 * for users now having access
	 */
	public class AddNotificationContext implements IConsumeNotificationFactory, IProvideNotificationFactory {
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

		@Override
		public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
			this.messageFactory = messageFactory;
		}

		@Override
		public void provideUsersToNotify(Set<String> users) {
			this.users = users;
		}
	}

}
