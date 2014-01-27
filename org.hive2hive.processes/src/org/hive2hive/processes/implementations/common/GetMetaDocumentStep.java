package org.hive2hive.processes.implementations.common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;

/**
 * Gets a {@link MetaFile} or a {@link MetaFolder} from the DHT and decrypts it.
 * 
 * @author Nico
 * 
 */
public class GetMetaDocumentStep extends BaseGetProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(GetMetaDocumentStep.class);
	private final IProvideMetaDocument metaContext;
	private final IConsumeKeyPair keyContext;

	public GetMetaDocumentStep(IConsumeKeyPair keyContext, IProvideMetaDocument metaContext,
			NetworkManager networkManager) {
		super(networkManager);
		this.keyContext = keyContext;
		this.metaContext = metaContext;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		KeyPair keyPair = keyContext.consumeKeyPair();
		NetworkContent content = get(keyPair.getPublic(), H2HConstants.META_DOCUMENT);
		if (content == null) {
			logger.warn("Meta document not found.");
			metaContext.provideMetaDocument(null);
		} else {
			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			try {
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, keyPair.getPrivate());
				decrypted.setVersionKey(content.getVersionKey());
				decrypted.setBasedOnKey(content.getBasedOnKey());

				metaContext.provideMetaDocument((MetaDocument) decrypted);
				logger.debug(String.format("Got and decrypted the meta document for file '%s'.",
						((MetaDocument) decrypted).getName()));
			} catch (IOException | ClassNotFoundException | InvalidKeyException | DataLengthException
					| IllegalBlockSizeException | BadPaddingException | IllegalStateException
					| InvalidCipherTextException | IllegalArgumentException e) {
				metaContext.provideMetaDocument(null);
				cancel(new RollbackReason(this, "Cannot decrypt the meta document."));
			}
		}
	}
}
