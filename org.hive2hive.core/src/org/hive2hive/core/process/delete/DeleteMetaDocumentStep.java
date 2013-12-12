package org.hive2hive.core.process.delete;

import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Deletes the meta document of the deleted file. After successful deletion, the entry is also removed from
 * the parent meta folder and then from the file tree in the user profile.
 * 
 * @author Nico
 * 
 */
public class DeleteMetaDocumentStep extends BaseRemoveProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteMetaDocumentStep.class);

	public DeleteMetaDocumentStep() {
		super(null);
	}

	@Override
	public void start() {
		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();
		MetaDocument metaDocument = context.getMetaDocument();

		nextStep = new GetParentMetaStep(metaDocument);

		try {
			// fist encrypt the content, in case of a rollback, we need to re-put this thing again
			HybridEncryptedContent encrypted = H2HEncryptionUtil.encryptHybrid(metaDocument,
					metaDocument.getId(), AES_KEYLENGTH.BIT_256);

			// start the deletion
			remove(key2String(metaDocument.getId()), H2HConstants.META_DOCUMENT, encrypted);
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			logger.warn("Cannot encrypt the meta document. Rollback may not be possible");
			remove(key2String(metaDocument.getId()), H2HConstants.META_DOCUMENT, null);
		}
	}
}
