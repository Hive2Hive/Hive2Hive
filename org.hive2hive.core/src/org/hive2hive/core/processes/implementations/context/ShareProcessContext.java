package org.hive2hive.core.processes.implementations.context;

import java.io.File;
import java.security.KeyPair;
import java.util.Set;

import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideIndex;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideNotificationFactory;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.processes.implementations.context.interfaces.IUpdateFileProtectionKey;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

public class ShareProcessContext implements IProvideProtectionKeys, IConsumeProtectionKeys, IProvideMetaFile,
		IConsumeMetaFile, IConsumeNotificationFactory, IProvideNotificationFactory, IUpdateFileProtectionKey,
		IProvideIndex {

	private final File folder;
	private final KeyPair newProtectionKeys;
	private final UserPermission permission;

	private KeyPair oldProtectionKeys;
	private MetaFile metaFile;
	private BaseNotificationMessageFactory messageFactory;
	private Set<String> users;
	private Index index;

	public ShareProcessContext(File folder, UserPermission permission) {
		this.folder = folder;
		this.permission = permission;
		this.newProtectionKeys = EncryptionUtil.generateProtectionKey();
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

	@Override
	public KeyPair consumeNewProtectionKeys() {
		return newProtectionKeys;
	}

	@Override
	public KeyPair consumeOldProtectionKeys() {
		return oldProtectionKeys;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return oldProtectionKeys;
	}

	/**
	 * Note that these are the old protection keys
	 */
	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.oldProtectionKeys = protectionKeys;
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore because only used for deletion
	}

	@Override
	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public void provideMessageFactory(BaseNotificationMessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	@Override
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

	@Override
	public void provideIndex(Index index) {
		this.index = index;
	}

	@Override
	public Index consumeIndex() {
		return index;
	}
}
