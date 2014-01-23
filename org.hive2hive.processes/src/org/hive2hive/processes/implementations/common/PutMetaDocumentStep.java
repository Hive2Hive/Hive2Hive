package org.hive2hive.processes.implementations.common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeProtectionKeys;

/**
 * Puts a {@link MetaFile} or a {@link MetaFolder} object into the DHT after encrypting it with the given key.
 * 
 * @author Nico
 * 
 */
public class PutMetaDocumentStep extends BasePutProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(PutMetaDocumentStep.class);

	private final IConsumeMetaDocument metaDocumentContext;
	private final IConsumeProtectionKeys protectionKeyContext;

	public PutMetaDocumentStep(IConsumeMetaDocument metaDocumentContext,
			IConsumeProtectionKeys protectionKeyContext, NetworkManager networkManager) {
		super(networkManager);
		this.metaDocumentContext = metaDocumentContext;
		this.protectionKeyContext = protectionKeyContext;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		try {
			MetaDocument metaDocument = metaDocumentContext.consumeMetaDocument();
			KeyPair protectionKeys = protectionKeyContext.consumeProtectionKeys();

			logger.debug("Encrypting meta document in a hybrid manner");
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(metaDocument,
					metaDocument.getId());
			encrypted.setBasedOnKey(metaDocument.getVersionKey());
			encrypted.generateVersionKey();
			put(key2String(metaDocument.getId()), H2HConstants.META_DOCUMENT, encrypted, protectionKeys);
		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			cancel(new RollbackReason(this, "Meta document could not be encrypted"));
		} catch (PutFailedException e) {
			cancel(new RollbackReason(this, e.getMessage()));
		}
	}
}
