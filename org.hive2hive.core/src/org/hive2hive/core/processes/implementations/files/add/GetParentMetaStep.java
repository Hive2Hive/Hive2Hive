package org.hive2hive.core.processes.implementations.files.add;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseGetProcessStep;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Gets the parent meta folder (if file is not in root) and the protection key for the new file.
 * 
 * @author Nico, Seppi
 * 
 */
public class GetParentMetaStep extends BaseGetProcessStep {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(GetParentMetaStep.class);

	private KeyPair parentsKeyPair;

	private final AddFileProcessContext context;

	public GetParentMetaStep(AddFileProcessContext context, IDataManager dataManager) {
		super(dataManager);
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// get and set the process context
		File file = context.getFile();
		File parent = file.getParentFile();

		if (context.isInRoot()) {
			// no parent to update since the file is in root
			logger.debug("File '" + file.getAbsolutePath() + "' is in root.");
			provideDefaultProtectionKeys();
		} else {
			// when file is not in root, the parent meta folder must be found
			logger.debug("File '" + file.getAbsolutePath() + "' is not in root.");
			getParentMetaFolder(parent);
		}
	}

	private void provideDefaultProtectionKeys() throws InvalidProcessStateException,
			ProcessExecutionException {
		try {
			KeyPair protectionKeys = context.getH2HSession().getProfileManager().getDefaultProtectionKey();
			context.provideProtectionKeys(protectionKeys);
			logger.debug("Got the default protection keys");
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("Default protection keys not found.", e);
		}
	}

	private void getParentMetaFolder(File parent) throws InvalidProcessStateException,
			ProcessExecutionException {

		UserProfileManager profileManager = context.getH2HSession().getProfileManager();
		UserProfile userProfile;
		try {
			userProfile = profileManager.getUserProfile(getID(), false);
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("Could not get the user profile.", e);
		}
		FileTreeNode parentNode = userProfile.getFileByPath(parent, context.getH2HSession().getFileManager());

		if (parentNode == null) {
			throw new ProcessExecutionException("Parent file is not in the user profile.");
		} else if (parentNode.getProtectionKeys() == null) {
			throw new ProcessExecutionException("This directory is write protected.");
		}

		context.provideProtectionKeys(parentNode.getProtectionKeys());
		parentsKeyPair = parentNode.getKeyPair();
		NetworkContent content = get(parentNode.getKeyPair().getPublic(), H2HConstants.META_DOCUMENT);
		evaluateResult(content);
	}

	private void evaluateResult(NetworkContent content) throws ProcessExecutionException {
		if (content == null) {
			logger.error("Meta document not found.");
			throw new ProcessExecutionException("Meta document not found.");
		} else {
			logger.debug("Got encrypted meta document");
			HybridEncryptedContent encrypted = (HybridEncryptedContent) content;
			try {
				NetworkContent decrypted = H2HEncryptionUtil.decryptHybrid(encrypted,
						parentsKeyPair.getPrivate());
				decrypted.setVersionKey(content.getVersionKey());
				decrypted.setBasedOnKey(content.getBasedOnKey());
				context.provideParentMetaDocument((MetaDocument) decrypted);
				logger.debug("Successfully decrypted meta document");
			} catch (IOException | ClassNotFoundException | InvalidKeyException | DataLengthException
					| IllegalBlockSizeException | BadPaddingException | IllegalStateException
					| InvalidCipherTextException | IllegalArgumentException e) {
				logger.error("Cannot decrypt the meta document.", e);
				throw new ProcessExecutionException("Cannot decrypt the meta document.", e);
			}
		}
	}
}
