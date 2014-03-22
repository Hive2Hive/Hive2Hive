package org.hive2hive.core.processes.implementations.common;

import java.io.File;
import java.security.KeyPair;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideKeyPair;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaFile;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;

/**
 * Finds a given file in the user profile and gets the appropriate meta data.
 * 
 * @author Nico, Seppi
 */
public class File2MetaFileComponent extends SequentialProcess {

	// TODO this class should not exist, but rather should a factory compose this component
	public File2MetaFileComponent(File file, IProvideMetaFile metaContext,
			IProvideProtectionKeys protectionContext, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		File2MetaContext file2MetaContext = new File2MetaContext();
		// first get the file keys, then get the meta file and protection keys
		add(new GetFileKeysStep(file, protectionContext, file2MetaContext, networkManager.getSession()));
		add(new GetMetaFileStep(file2MetaContext, metaContext, networkManager.getDataManager()));
	}

	public File2MetaFileComponent(Index fileNode, IProvideMetaFile metaContext,
			IProvideProtectionKeys protectionContext, IDataManager dataManager) {
		// already fill the context because the index is already present
		File2MetaContext file2MetaContext = new File2MetaContext();
		protectionContext.provideProtectionKeys(fileNode.getProtectionKeys());
		file2MetaContext.provideKeyPair(fileNode.getFileKeys());

		add(new GetMetaFileStep(file2MetaContext, metaContext, dataManager));
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
