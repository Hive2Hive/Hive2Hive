package org.hive2hive.core.process.context;

import java.security.KeyPair;

import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.security.HybridEncryptedContent;

public interface IGetParentMetaContext {

	public MetaFolder getParentMetaFolder();

	public void setParentMetaFolder(MetaFolder parentMetaFolder);

	public HybridEncryptedContent getEncryptedParentMetaFolder();
	
	public void setEncryptedParentMetaFolder(HybridEncryptedContent encryptedParentMetaFolder);
	
	public KeyPair getParentProtectionKeys();
	
	public void setParentProtectionKeys(KeyPair parentProtectionKeys);
}
