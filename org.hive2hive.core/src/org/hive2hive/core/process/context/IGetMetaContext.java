package org.hive2hive.core.process.context;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.security.HybridEncryptedContent;

public interface IGetMetaContext {

	void setMetaDocument(MetaDocument metaDocument);

	MetaDocument getMetaDocument();
	
	void setEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument);

	HybridEncryptedContent getEncryptedMetaDocument();
	
	void setProtectionKeys(KeyPair protectionKeys);
	
	KeyPair getProtectionKeys();
}
