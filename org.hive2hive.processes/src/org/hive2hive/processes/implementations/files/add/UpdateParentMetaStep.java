package org.hive2hive.processes.implementations.files.add;

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
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.common.base.BasePutProcessStep;
import org.hive2hive.processes.implementations.context.AddFileProcessContext;

public class UpdateParentMetaStep extends BasePutProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(UpdateParentMetaStep.class);
	private final AddFileProcessContext context;

	public UpdateParentMetaStep(AddFileProcessContext context, NetworkManager networkManager) {
		super(networkManager);
		this.context = context;
	}

	@Override
	public void doExecute() throws InvalidProcessStateException {
		logger.debug(String.format("Start updating the parent meta folder of file: %s", context.getFile()
				.getName()));

		// add child to the parent meta data
		MetaFolder parentMeta = (MetaFolder) context.consumeParentMetaDocument();
		if (parentMeta == null) {
			cancel(new RollbackReason(this, "Could not find the parent meta data."));
			return;
		}

		KeyPair protectionKeys = (KeyPair) context.consumeProtectionKeys();
		if (protectionKeys == null) {
			cancel(new RollbackReason(this, "No protection keys are set."));
			return;
		}

		parentMeta.addChildKeyPair(context.getNewMetaKeyPair());
		logger.debug(String.format("MetaFolder has new child. Total children = %s", parentMeta.getChildKeys()
				.size()));

		try {
			logger.debug("Encrypting meta document in a hybrid manner");
			HybridEncryptedContent encrypted = H2HEncryptionUtil
					.encryptHybrid(parentMeta, parentMeta.getId());
			encrypted.setBasedOnKey(parentMeta.getVersionKey());
			encrypted.generateVersionKey();
			put(parentMeta.getId(), H2HConstants.META_DOCUMENT, encrypted, protectionKeys);
		} catch (IOException | DataLengthException | InvalidKeyException | IllegalStateException
				| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
			cancel(new RollbackReason(this, "Parent meta document could not be encrypted"));
		} catch (PutFailedException e) {
			cancel(new RollbackReason(this, "Parent meta document could not be put"));
		}
	}
}
