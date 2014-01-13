package org.hive2hive.core.process.upload.newfile;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.upload.UploadFileProcessContext;

public class NewFileProcessContext extends UploadFileProcessContext {

	private KeyPair newKeyPair;
	private MetaDocument newMetaDocument;

	public NewFileProcessContext(NewFileProcess process, File file, H2HSession session) {
		super(process, file, session, false);
	}

	public KeyPair getNewMetaKeyPair() {
		return newKeyPair;
	}

	public void setNewMetaKeyPair(KeyPair newKeyPair) {
		this.newKeyPair = newKeyPair;
	}
	
	public MetaDocument getNewMetaDocument(){
		return newMetaDocument;
	}
	
	public void setNewMetaDocument(MetaDocument newMetaDocument){
		this.newMetaDocument = newMetaDocument;
	}
	
}
