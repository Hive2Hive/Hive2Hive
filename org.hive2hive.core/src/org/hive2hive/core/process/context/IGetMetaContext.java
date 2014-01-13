package org.hive2hive.core.process.context;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaDocument;

public interface IGetMetaContext {

	void setMetaDocument(MetaDocument metaDocument);

	MetaDocument getMetaDocument();
	
	void setProtectionKeys(KeyPair protectionKeys);
	
	KeyPair getProtectionKeys();
}
