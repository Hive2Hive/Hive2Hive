package org.hive2hive.core.processes.implementations.share.pkupdate;

import java.util.List;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.File2MetaFileComponent;
import org.hive2hive.core.processes.implementations.context.MetaDocumentPKUpdateContext;
import org.hive2hive.core.processes.implementations.context.interfaces.IUpdateFileProtectionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes the shared folder and iteratively changes the protection keys of all meta files. Appends further
 * steps to change the content protection key of all contained chunks.
 * 
 * @author Nico, Seppi
 */
public class InitializeMetaUpdateStep extends ProcessStep {

	private final static Logger logger = LoggerFactory.getLogger(InitializeMetaUpdateStep.class);

	private final IUpdateFileProtectionKey context;
	private final IDataManager dataManager;

	public InitializeMetaUpdateStep(IUpdateFileProtectionKey context, IDataManager dataManager) {
		this.context = context;
		this.dataManager = dataManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		Index index = context.consumeIndex();

		try {
			if (index.isFolder()) {
				FolderIndex folderIndex = (FolderIndex) index;
				initForFolder(folderIndex);
			} else {
				FileIndex fileIndex = (FileIndex) index;
				initForFile(fileIndex);
			}
		} catch (NoSessionException | NoPeerConnectionException e) {
			throw new ProcessExecutionException(e);
		}

	}

	private void initForFolder(FolderIndex folderIndex) throws ProcessExecutionException, NoSessionException,
			NoPeerConnectionException {
		List<Index> indexList = Index.getIndexList(folderIndex);
		for (Index index : indexList) {
			if (index.isFile()) {
				FileIndex fileIndex = (FileIndex) index;
				initForFile(fileIndex);
			}
		}
	}

	private void initForFile(FileIndex fileIndex) throws NoSessionException, NoPeerConnectionException {
		logger.debug("Initialize to change the protection keys of meta document of index '{}'.",
				fileIndex.getName());
		// create the process and make wrap it to make it asynchronous
		getParent().add(new AsyncComponent(buildProcess(fileIndex)));
	}

	private ProcessComponent buildProcess(FileIndex index) throws NoSessionException,
			NoPeerConnectionException {
		// create a new sub-process
		SequentialProcess sequential = new SequentialProcess();

		// each meta document gets own context
		MetaDocumentPKUpdateContext metaContext = new MetaDocumentPKUpdateContext(
				context.consumeOldProtectionKeys(), context.consumeNewProtectionKeys(),
				index.getFilePublicKey(), index);
		sequential.add(new File2MetaFileComponent(index, metaContext, metaContext, dataManager));
		sequential.add(new ChangeProtectionKeysStep(metaContext, dataManager));
		sequential.add(new InitializeChunkUpdateStep(metaContext, dataManager));
		return sequential;
	}
}
