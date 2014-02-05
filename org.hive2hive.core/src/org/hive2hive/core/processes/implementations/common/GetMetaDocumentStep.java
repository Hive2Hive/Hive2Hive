package org.hive2hive.core.processes.implementations.common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Gets a {@link MetaFile} or a {@link MetaFolder} from the DHT and decrypts it.
 * 
 * @author Nico
 * 
 */
public class GetMetaDocumentStep extends BaseGetProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(GetMetaDocumentStep.class);

	private final IConsumeKeyPair keyContext;
	private final IProvideMetaDocument metaContext;

	public GetMetaDocumentStep(IConsumeKeyPair keyContext, IProvideMetaDocument metaContext,
			IDataManager dataManager) {
		super(dataManager);
		this.keyContext = keyContext;
		this.metaContext = metaContext;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		KeyPair keyPair = keyContext.consumeKeyPair();
		NetworkContent loadedContent = get(keyPair.getPublic(), H2HConstants.META_DOCUMENT);

		if (loadedContent == null) {
			logger.warn("Meta document not found.");
			cancel(new RollbackReason(this, "Meta document not found."));
		} else {

			// decrypt meta document
			HybridEncryptedContent encryptedContent = (HybridEncryptedContent) loadedContent;

			NetworkContent decryptedContent = null;
			try {
				decryptedContent = H2HEncryptionUtil.decryptHybrid(encryptedContent, keyPair.getPrivate());
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
					| BadPaddingException | IllegalStateException | InvalidCipherTextException
					| ClassNotFoundException | IOException e) {
				cancel(new RollbackReason(this, "Meta document could not be decrypted. Reason: "
						+ e.getMessage()));
				return;
			}

			MetaDocument metaDocument = (MetaDocument) decryptedContent;
			metaDocument.setVersionKey(loadedContent.getVersionKey());
			metaDocument.setBasedOnKey(loadedContent.getBasedOnKey());

			metaContext.provideMetaDocument(metaDocument);
			metaContext.provideEncryptedMetaDocument(encryptedContent);
			logger.debug(String.format("Got and decrypted the meta document for file '%s'.",
					metaDocument.getName()));
		}
	}
}
