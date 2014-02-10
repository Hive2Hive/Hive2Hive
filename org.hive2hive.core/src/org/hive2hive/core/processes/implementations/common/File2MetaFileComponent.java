package org.hive2hive.core.processes.implementations.common;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;

/**
 * Finds a given file in the user profile and gets the appropriate meta data.
 * 
 * @author Nico, Seppi
 */
public class File2MetaFileComponent extends SequentialProcess {

	// TODO this class should not exist, but rather should a factory compose this component
	// TODO this class needs some refactoring
	public File2MetaFileComponent(File file, IProvideMetaDocument metaContext,
			IProvideProtectionKeys protectionContext, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		this(file, null, metaContext, protectionContext, networkManager);
	}

	public File2MetaFileComponent(Index fileNode, IProvideMetaDocument metaContext,
			IProvideProtectionKeys protectionContext, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		this(null, fileNode, metaContext, protectionContext, networkManager);
	}

	private File2MetaFileComponent(File file, Index fileNode, IProvideMetaDocument metaContext,
			IProvideProtectionKeys protectionContext, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		File2MetaContext file2MetaContext = new File2MetaContext();

		// first get the protection a
		if (fileNode == null) {
			add(new GetFileKeysStep(file, protectionContext, file2MetaContext, networkManager));
		} else {
			protectionContext.provideProtectionKeys(fileNode.getProtectionKeys());
			file2MetaContext.provideKeyPair(fileNode.getFileKeys());
		}

		add(new GetMetaDocumentStep(file2MetaContext, metaContext, networkManager.getDataManager()));
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
