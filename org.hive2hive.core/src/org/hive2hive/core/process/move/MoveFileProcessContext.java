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

	private final File source;
	private final File destination;
	private final Set<String> usersToNotifySource;
	private final Set<String> usersToNotifyDestination;
	
	private MetaDocument metaDocument;
	private KeyPair protectionKeys;
	private KeyPair nodeKeyPair;
	private KeyPair destinationParentKeys;
	private KeyPair sourceProtectionKeys;
	private KeyPair destinationProtectionKeys;

	public MoveFileProcessContext(Process process, File source, File destination) {
		super(process);
		this.source = source;
		this.destination = destination;
		this.usersToNotifySource = new HashSet<String>();
		usersToNotifySource.add(process.getNetworkManager().getUserId());
		this.usersToNotifyDestination = new HashSet<String>();
		usersToNotifyDestination.add(process.getNetworkManager().getUserId());
	}

	@Override
	public void setMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument getMetaDocument() {
		return metaDocument;
	}
	
	@Override
	public KeyPair getProtectionKeys() {
		return protectionKeys;
	}

	@Override
	public void setProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
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

	/**
	 * The keys of the destination's parent. However, if the destination is the root, the keys are null
	 */
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
	
	public KeyPair getSourceProtectionKeys() {
		return sourceProtectionKeys;
	}

	public void setSourceProtectionKeys(KeyPair sourceProtectionKeys) {
		this.sourceProtectionKeys = sourceProtectionKeys;
	}
	
	public KeyPair getDestinationProtectionKeys() {
		return destinationProtectionKeys;
	}

	public void setDestinationProtectionKeys(KeyPair destinationProtectionKeys) {
		this.destinationProtectionKeys = destinationProtectionKeys;
	}
}
