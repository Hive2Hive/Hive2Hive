package org.hive2hive.core.process.move;

import java.io.File;
import java.security.KeyPair;
import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class MoveFileProcessContext extends ProcessContext implements IGetMetaContext {

	private MetaDocument metaDocument;
	private final File source;
	private final File destination;
	private final Set<String> usersToNotifySource;
	private final Set<String> usersToNotifyDestination;
	private KeyPair nodeKeyPair;
	private KeyPair destinationParentKeys;

	public MoveFileProcessContext(Process process, File source, File destination) {
		super(process);
		this.source = source;
		this.destination = destination;
		this.usersToNotifySource = new HashSet<String>();
		this.usersToNotifyDestination = new HashSet<String>();
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}

	public File getSource() {
		return source;
	}

	public File getDestination() {
		return destination;
	}

	public void setFileNodeKeys(KeyPair nodeKeyPair) {
		this.nodeKeyPair = nodeKeyPair;
	}

	public KeyPair getFileNodeKeys() {
		return nodeKeyPair;
	}

	public void setDestinationParentKeys(KeyPair destinationParentKeys) {
		this.destinationParentKeys = destinationParentKeys;
	}

	public KeyPair getDestinationParentKeys() {
		return destinationParentKeys;
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
}
