package org.hive2hive.core.process.delete;

import java.security.KeyPair;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.IGetParentMetaContext;
import org.hive2hive.core.process.context.ProcessContext;
import org.hive2hive.core.security.HybridEncryptedContent;

public class DeleteFileProcessContext extends ProcessContext implements IGetMetaContext,
		IGetParentMetaContext {

	private final H2HSession session;
	private final boolean isDirectory;

	private MetaDocument metaDocument;
	private HybridEncryptedContent encryptedMetaDocument;
	private KeyPair protectionKeys;
	private MetaFolder parentMetaFolder;
	private HybridEncryptedContent encryptedParentMetaFolder;
	private KeyPair parentProtectionKeys;

	public DeleteFileProcessContext(H2HSession session, boolean isDirectory, Process process) {
		super(process);
		this.session = session;
		this.isDirectory = isDirectory;
	}

	public H2HSession getH2HSession() {
		return session;
	}

	public boolean isDirectory() {
		return isDirectory;
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
	public void setEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		this.encryptedMetaDocument = encryptedMetaDocument;
	}

	@Override
	public HybridEncryptedContent getEncryptedMetaDocument() {
		return encryptedMetaDocument;
	}

	@Override
	public KeyPair getProtectionKeys() {
		return protectionKeys;
	}

	@Override
	public void setProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public void setParentMetaFolder(MetaFolder parentMetaFolder) {
		this.parentMetaFolder = parentMetaFolder;
	}

	@Override
	public MetaFolder getParentMetaFolder() {
		return parentMetaFolder;
	}

	@Override
	public HybridEncryptedContent getEncryptedParentMetaFolder() {
		return encryptedParentMetaFolder;
	}

	@Override
	public void setEncryptedParentMetaFolder(HybridEncryptedContent encryptedParentMetaFolder) {
		this.encryptedParentMetaFolder = encryptedParentMetaFolder;
	}

	@Override
	public KeyPair getParentProtectionKeys() {
		return parentProtectionKeys;
	}

	@Override
	public void setParentProtectionKeys(KeyPair parentProtectionKeys) {
		this.parentProtectionKeys = parentProtectionKeys;
	}
}
