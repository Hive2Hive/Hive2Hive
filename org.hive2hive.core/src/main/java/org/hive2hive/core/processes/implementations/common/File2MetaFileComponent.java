package org.hive2hive.core.processes.implementations.common;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.implementations.context.interfaces.common.IFile2MetaContext;

/**
 * Finds a given file in the user profile and gets the appropriate meta data.
 * 
 * @author Nico, Seppi
 */
public class File2MetaFileComponent extends SequentialProcess {

	public File2MetaFileComponent(IFile2MetaContext context, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		// first get the file keys, then get the meta file and protection keys
		add(new GetFileKeysStep(context, networkManager.getSession()));
		add(new GetMetaFileStep(context, networkManager.getDataManager()));
	}

	public File2MetaFileComponent(Index fileNode, IFile2MetaContext context, IDataManager dataManager) {
		// already fill the context because the index is already present
		context.provideProtectionKeys(fileNode.getProtectionKeys());
		context.provideMetaFileEncryptionKeys(fileNode.getFileKeys());
		add(new GetMetaFileStep(context, dataManager));
	}

}
