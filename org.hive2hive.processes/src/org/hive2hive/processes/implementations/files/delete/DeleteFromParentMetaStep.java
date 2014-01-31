package org.hive2hive.processes.implementations.files.delete;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.PutFailedException;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.delete.DeleteNotifyMessageFactory;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.processes.implementations.context.DeleteFileProcessContext;

// TODO implement rollback

public class DeleteFromParentMetaStep extends BasePutProcessStep {

	private final DeleteFileProcessContext context;

	public DeleteFromParentMetaStep(DeleteFileProcessContext context, NetworkManager networkManager) {
		super(networkManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		// check preconditions
		if (context.getIsInRootFile()) {
			return;
		}
		if (context.getParentMetaFolder() == null) {
			cancel(new RollbackReason(this, "Parent meta folder is not given."));
			return;
		}
		if (context.getChildNode() == null) {
			cancel(new RollbackReason(this, "Child node is not given."));
			return;
		}
		if (context.getParentNode() == null) {
			cancel(new RollbackReason(this, "Parent node is not given."));
			return;
		}
		if (context.getParentNode().getProtectionKeys() == null) {
			cancel(new RollbackReason(this, "Parent protection keys are null."));
			return;
		}
		if (context.consumeMetaDocument() == null) {
			cancel(new RollbackReason(this, "Meta document is null."));
			return;
		}

		MetaFolder parentMetaFolder = context.getParentMetaFolder();

		// update parent meta folder (delete child)
		parentMetaFolder.removeChildKey(context.consumeMetaDocument().getId());

		// TODO send notifications (if everything finished successfully?)
		DeleteNotifyMessageFactory messageFactory = new DeleteNotifyMessageFactory(context
				.consumeMetaDocument().getId(), parentMetaFolder.getId(), context.getChildNode().getName());
		// getProcess().sendNotification(messageFactory, parentMeta.getUserList());

		// encrypt updated parent meta folder
		HybridEncryptedContent encryptedContent = null;
		try {
			encryptedContent = H2HEncryptionUtil.encryptHybrid(parentMetaFolder,
					parentMetaFolder.getId());
		} catch (DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException | IOException e) {
			cancel(new RollbackReason(this, "Parent meta folder could not be encrypted " + e.getMessage()));
			return;
		}
		
		// put updated parent meta folder
		encryptedContent.setBasedOnKey(parentMetaFolder.getBasedOnKey());
		try {
			encryptedContent.generateVersionKey();
		} catch (IOException e) {
			cancel(new RollbackReason(this, "Could not generate version keys."));
			return;
		}
		
		try {
			put(parentMetaFolder.getId(), H2HConstants.META_DOCUMENT, encryptedContent, context.getParentProtectionKeys());
		} catch (PutFailedException e) {
			cancel(new RollbackReason(this, "Put failed."));
		}
	}

}