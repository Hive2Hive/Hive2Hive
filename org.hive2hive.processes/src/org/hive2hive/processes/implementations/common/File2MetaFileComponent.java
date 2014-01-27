package org.hive2hive.processes.implementations.common;

import java.io.File;
import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.login.GetUserProfileStep;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.concretes.SequentialProcess;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.processes.implementations.context.interfaces.IProvideKeyPair;
import org.hive2hive.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.processes.implementations.context.interfaces.IProvideProtectionKeys;

/**
 * Finds a given file in the user profile and gets the appropriate meta data. Note that a
 * {@link GetUserProfileStep} must be run before this step is run.
 * 
 * @author Nico, Seppi
 */
public class File2MetaFileComponent extends SequentialProcess {

	private final static Logger logger = H2HLoggerFactory.getLogger(File2MetaFileComponent.class);

	private final File file;
	private final UserProfileManager profileManager;
	private final FileManager fileManager;

	public KeyPair fileKey;

	public File2MetaFileComponent(File file, IProvideMetaDocument metaContext,
			IProvideProtectionKeys protectionContext, NetworkManager networkManager)
			throws NoSessionException {
		this(file, null, metaContext, protectionContext, networkManager);
	}

	public File2MetaFileComponent(FileTreeNode fileNode, IProvideMetaDocument metaContext,
			IProvideProtectionKeys protectionContext, NetworkManager networkManager)
			throws NoSessionException {
		this(null, fileNode, metaContext, protectionContext, networkManager);
	}

	private File2MetaFileComponent(File file, FileTreeNode fileNode, IProvideMetaDocument metaContext,
			IProvideProtectionKeys protectionContext, NetworkManager networkManager)
			throws NoSessionException {
		this.file = file;
		this.profileManager = networkManager.getSession().getProfileManager();
		this.fileManager = networkManager.getSession().getFileManager();

		File2MetaContext file2MetaContext = new File2MetaContext();

		// first get the protection a
		if (fileNode == null) {
			add(new GetFileKeysStep(protectionContext, file2MetaContext));
		} else {
			protectionContext.provideProtectionKeys(fileNode.getProtectionKeys());
			file2MetaContext.provideKeyPair(fileNode.getKeyPair());
		}

		add(new GetMetaDocumentStep(file2MetaContext, metaContext, networkManager));
	}

	/**
	 * Gets the file keys (and their protection keys)
	 * 
	 * @author Nico
	 * 
	 */
	private class GetFileKeysStep extends ProcessStep {

		private final IProvideProtectionKeys protectionContext;
		private final File2MetaContext file2MetaContext;

		public GetFileKeysStep(IProvideProtectionKeys protectionContext, File2MetaContext file2MetaContext) {
			this.protectionContext = protectionContext;
			this.file2MetaContext = file2MetaContext;
		}

		@Override
		protected void doExecute() throws InvalidProcessStateException {
			// file node can be null or already present
			logger.info(String.format("Getting the corresponding file node for file '%s'.", file.getName()));

			// file node is null, first look it up in the user profile
			UserProfile profile = null;
			try {
				profile = profileManager.getUserProfile(getID(), false);
			} catch (GetFailedException e) {
				cancel(new RollbackReason(this, e.getMessage()));
				return;
			}

			FileTreeNode fileNode = profile.getFileByPath(file, fileManager);
			if (fileNode == null) {
				cancel(new RollbackReason(this,
						"File does not exist in user profile. Consider uploading a new file."));
				return;
			}

			// set the corresponding content protection keys
			protectionContext.provideProtectionKeys(fileNode.getProtectionKeys());
			file2MetaContext.provideKeyPair(fileNode.getKeyPair());
		}
	}

	private class File2MetaContext implements IProvideKeyPair, IConsumeKeyPair {

		private KeyPair keyPair;

		@Override
		public void provideKeyPair(KeyPair keyPair) {
			this.keyPair = keyPair;

		}

		@Override
		public KeyPair consumeKeyPair() {
			return keyPair;
		}

	}
}
