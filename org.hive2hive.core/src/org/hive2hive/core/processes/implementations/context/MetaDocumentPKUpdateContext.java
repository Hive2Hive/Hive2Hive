package org.hive2hive.core.processes.implementations.context;

import java.security.KeyPair;
import java.security.PublicKey;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.TimeToLiveStore;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Provides the required context to update the meta document
 */
public class MetaDocumentPKUpdateContext extends BasePKUpdateContext implements IProvideProtectionKeys,
		IProvideMetaFile, IConsumeMetaFile {

	private final PublicKey fileKey;
	private MetaFile metaFile;

	public MetaDocumentPKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys, PublicKey fileKey) {
		super(oldProtectionKeys, newProtectionKeys);
		this.fileKey = fileKey;
	}

	@Override
	public void provideProtectionKeys(KeyPair protectionKeys) {
		// ignore because this is the old protection key which we have already
	}

	@Override
	public void provideMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	@Override
	public void provideEncryptedMetaFile(HybridEncryptedContent encryptedMetaDocument) {
		// ignore
	}

	@Override
	public MetaFile consumeMetaFile() {
		return metaFile;
	}

	@Override
	public String getLocationKey() {
		return H2HEncryptionUtil.key2String(fileKey);
	}

	@Override
	public String getContentKey() {
		return H2HConstants.META_FILE;
	}

	@Override
	public int getTTL() {
		return TimeToLiveStore.getInstance().getMetaFile();
	}
}