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

	private final KeyPair keyPair;
	private final ProcessStep nextStep;

	private MetaDocument metaDocument;

	public GetMetaDocumentStep(KeyPair keyPair, ProcessStep nextStep) {
		super(key2String(keyPair.getPublic()), H2HConstants.META_DOCUMENT);
		this.keyPair = keyPair;
		this.nextStep = nextStep;
	}

	@Override
	protected void handleGetResult(NetworkContent content) {
		HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
		try {
			NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted, keyPair.getPrivate());
			this.metaDocument = (MetaDocument) decrypted;
		} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException | BadPaddingException
				| IllegalStateException | InvalidCipherTextException e) {
			logger.error("Cannot decrypt the meta document.", e);
		}

		// continue with next step
		getProcess().setNextStep(nextStep);
	}

	public MetaDocument getMetaDocument() {
		return metaDocument;
	}
}
