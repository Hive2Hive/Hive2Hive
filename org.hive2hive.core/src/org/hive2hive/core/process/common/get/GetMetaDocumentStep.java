package org.hive2hive.core.process.common.get;

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
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.context.IGetMetaContext;
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

	protected KeyPair keyPair;
	protected ProcessStep nextStep;
	protected IGetMetaContext context;
	private String key = null;

	public GetMetaDocumentStep(KeyPair keyPair, ProcessStep nextStep, IGetMetaContext context) {
		this.keyPair = keyPair;
		this.nextStep = nextStep;
		this.context = context;
	}

	@Override
	public void start() {
		key = key2String(keyPair.getPublic());
		logger.debug("Get meta document for key " + key);
		get(key, H2HConstants.META_DOCUMENT);
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		if (content == null) {
			logger.error("Meta document not found");
			context.setMetaDocument(null);
		} else {
			logger.debug("Got encrypted meta document " + key);
			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			try {
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, keyPair.getPrivate());
				context.setMetaDocument((MetaDocument) decrypted);
				logger.debug("Successfully decrypted meta document " + key);
			} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
					| BadPaddingException | IllegalStateException | InvalidCipherTextException
					| IllegalArgumentException e) {
				logger.error("Cannot decrypt the meta document.", e);
				context.setMetaDocument(null);
			}
		}
		// continue with next step
		getProcess().setNextStep(nextStep);
	}

}
