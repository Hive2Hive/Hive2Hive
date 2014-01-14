package org.hive2hive.core.process.recover;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;
import org.hive2hive.core.security.HybridEncryptedContent;

public class RecoverFileProcessContext extends ProcessContext implements IGetMetaContext {

	private final IVersionSelector versionSelector;
	private final File file;
	
	private FileVersion version;
	private MetaDocument metaDocument;
	private HybridEncryptedContent encryptedMetaDocument;
	private KeyPair protectionKeys;

	public RecoverFileProcessContext(Process process, File file, IVersionSelector versionSelector) {
		super(process);
		this.file = file;
		this.versionSelector = versionSelector;
	}

	public IVersionSelector getVersionSelector() {
		return versionSelector;
	}

	public void setSelectedFileVersion(FileVersion version) {
		this.version = version;
	}

	public FileVersion getSelectedFileVersion() {
		return version;
	}

	public File getFile() {
		return file;
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
	public void setProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}

	@Override
	public KeyPair getProtectionKeys() {
		return protectionKeys;
	}
	
}
