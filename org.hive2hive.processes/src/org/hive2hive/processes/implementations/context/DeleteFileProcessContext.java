package org.hive2hive.processes.implementations.context;

import java.security.KeyPair;

import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.context.IGetParentMetaContext;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeProtectionKeys;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IProvideProtectionKeys;

public class DeleteFileProcessContext implements IProvideMetaDocument, IConsumeMetaDocument, IProvideProtectionKeys, IConsumeProtectionKeys, IGetParentMetaContext {

	// TODO refactor this class, interfaces
	
	private final boolean isDirectory;

	private MetaDocument metaDocument;
	private KeyPair protectionKeys;
	private HybridEncryptedContent encryptedMetaDocument;

	private MetaFolder parentMetaFolder;
	private KeyPair parentProtectionKeys;
	private HybridEncryptedContent encryptedParentMetaFolder;

	private boolean isInRoot;

	private FileTreeNode parentNode;

	private FileTreeNode fileNode;
	
	public DeleteFileProcessContext(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	
	@Override
	public void provideMetaDocument(MetaDocument metaDocument) {
		this.metaDocument = metaDocument;
	}

	@Override
	public MetaDocument consumeMetaDocument() {
		return metaDocument;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public KeyPair consumeProtectionKeys() {
		return protectionKeys;
	}

	public void setEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
		this.encryptedMetaDocument = encryptedMetaDocument;
	}

	public HybridEncryptedContent getEncryptedMetaDocument() {
		return encryptedMetaDocument;
	}

	public boolean isDirectory() {
		return isDirectory;
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
	public void setParentProtectionKeys(KeyPair parentProtectionKeys) {
		this.parentProtectionKeys = parentProtectionKeys;
	}

	@Override
	public KeyPair getParentProtectionKeys() {
		return parentProtectionKeys;
	}

	@Override
	public void setEncryptedParentMetaFolder(HybridEncryptedContent encryptedParentMetaFolder) {
		this.encryptedParentMetaFolder = encryptedParentMetaFolder;
	}

	@Override
	public HybridEncryptedContent getEncryptedParentMetaFolder() {
		return encryptedParentMetaFolder;
	}

	public void setIsInRootFile(boolean isInRoot) {
		this.isInRoot = isInRoot;
	}
	
	public boolean getIsInRootFile() {
		return isInRoot;
	}

	public void setChildNode(FileTreeNode fileNode) {
		this.fileNode = fileNode;
	}

	public FileTreeNode getChildNode() {
		return fileNode;
	}

	public void setParentNode(FileTreeNode parentNode) {
		this.parentNode = parentNode;
	}
	
	public FileTreeNode getParentNode() {
		return parentNode;
	}
}
