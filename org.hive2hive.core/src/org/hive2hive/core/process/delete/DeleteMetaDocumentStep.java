package org.hive2hive.core.process.delete;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.RemoveFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Deletes the meta document of the deleted file. After successful deletion, the entry is also removed from
 * the parent meta folder and then from the file tree in the user profile.
 * 
 * @author Nico, Seppi
 */
public class DeleteMetaDocumentStep extends BaseRemoveProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DeleteMetaDocumentStep.class);

	@Override
	public void start() {
		DeleteFileProcessContext context = (DeleteFileProcessContext) getProcess().getContext();
		MetaDocument metaDocument = context.getMetaDocument();
		if (metaDocument == null) {
			getProcess().stop("Meta document is null.");
			return;
		}
		HybridEncryptedContent encryptedMetaDocument = context.getEncryptedMetaDocument();
		if (encryptedMetaDocument == null) {
			getProcess().stop("Encrypted meta document is null.");
			return;
		}
		KeyPair protectionKeys = context.getProtectionKeys();
		if (protectionKeys == null) {
			getProcess().stop("No content protection keys (no write permission).");
			return;
		}

		logger.debug(String.format("Deleting the meta document of file '%s'.", metaDocument.getName()));

		// start the deletion
		try {
			remove(key2String(metaDocument.getId()), H2HConstants.META_DOCUMENT, encryptedMetaDocument,
					protectionKeys);
			getProcess().setNextStep(new DeleteGetParentMetaStep());
		} catch (RemoveFailedException e) {
			getProcess().stop(e);
		}
	}
}
