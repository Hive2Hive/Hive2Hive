package org.hive2hive.core.process.upload;

import java.io.File;
import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.IGetMetaContext;
import org.hive2hive.core.process.context.ProcessContext;

public class UploadFileProcessContext extends ProcessContext implements IGetMetaContext {

	private final File file;
	private final H2HSession session;
	private final boolean fileAlreadyExists;
	
	private List<KeyPair> chunkKeys;
	private MetaDocument metaDocument;
	private KeyPair protectionKeys;
	
	public UploadFileProcessContext(Process process, File file, H2HSession session, boolean fileAlreadyExists) {
		super(process);
		this.file = file;
		this.session = session;
		this.fileAlreadyExists = fileAlreadyExists;
	}

	public File getFile() {
		return file;
	}

	public H2HSession getH2HSession() {
		return session;
	}
	
	public boolean getFileAlreadyExists() {
		return fileAlreadyExists;
	}

	public void setChunkKeys(List<KeyPair> chunkKeys) {
		this.chunkKeys = chunkKeys;
	}

	public List<KeyPair> getChunkKeys() {
		return chunkKeys;
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
	public void setProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
	}
	
	@Override
	public KeyPair getProtectionKeys() {
		return protectionKeys;
	}
	
}
