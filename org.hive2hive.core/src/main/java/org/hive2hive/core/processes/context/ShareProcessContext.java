package org.hive2hive.core.processes.context;

import java.io.File;
import java.security.KeyPair;
import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.versioned.BaseMetaFile;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.processes.context.interfaces.IInitializeMetaUpdateContext;
import org.hive2hive.core.processes.context.interfaces.INotifyContext;
import org.hive2hive.core.processes.notify.BaseNotificationMessageFactory;

public class ShareProcessContext implements IInitializeMetaUpdateContext, INotifyContext {

	private final File folder;
	private final UserPermission permission;

	private KeyPair oldProtectionKeys;
	private KeyPair newProtectionKeys;
	private BaseMetaFile metaFile;
	private BaseNotificationMessageFactory messageFactory;
	private Set<String> users;
	private Index index;

	public ShareProcessContext(File folder, UserPermission permission) {
		this.folder = folder;
		this.permission = permission;
	}

	public File getFolder() {
		return folder;
	}

	public String getFriendId() {
		return permission.getUserId();
	}

	public PermissionType getPermissionType() {
		return permission.getPermission();
	}

	public UserPermission getUserPermission() {
		return permission;
	}

	public void provideNewProtectionKeys(KeyPair newProtectionKeys) {
		this.newProtectionKeys = newProtectionKeys;
	}

	@Override
	public KeyPair consumeNewProtectionKeys() {
		return newProtectionKeys;
	}

	@Override
	public KeyPair consumeOldProtectionKeys() {
		return oldProtectionKeys;
	}

	public KeyPair consumeProtectionKeys() {
		return oldProtectionKeys;
	}

	/**
	 * Note that these are the old protection keys
	 */
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.oldProtectionKeys = protectionKeys;
	}

	public void provideMetaFile(BaseMetaFile metaFile) {
		this.metaFile = metaFile;
	}

	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}

	public BaseMetaFile consumeMetaFile() {
		return metaFile;
	}

	public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	public void provideUsersToNotify(Set<String> users) {
		this.users = users;
	}

	@Override
	public BaseNotificationMessageFactory consumeMessageFactory() {
		return messageFactory;
	}

	@Override
	public Set<String> consumeUsersToNotify() {
		return users;
	}

	public void provideIndex(Index index) {
		this.index = index;
	}

	@Override
	public Index consumeIndex() {
		return index;
	}
}
