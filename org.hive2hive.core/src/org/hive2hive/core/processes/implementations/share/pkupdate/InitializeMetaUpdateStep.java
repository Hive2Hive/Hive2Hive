package org.hive2hive.core.processes.implementations.share.pkupdate;

import java.util.List;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.File2MetaFileComponent;
import org.hive2hive.core.processes.implementations.context.MetaDocumentPKUpdateContext;
import org.hive2hive.core.processes.implementations.context.ShareProcessContext;

/**
 * Takes the shared folder and iteratively changes the protection keys of all meta documents
 * 
 * @author Nico
 * 
 */
public class InitializeMetaUpdateStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(InitializeMetaUpdateStep.class);

	private ShareProcessContext context;
	private NetworkManager networkManager;

	public InitializeMetaUpdateStep(ShareProcessContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		FolderIndex folderIndex = (FolderIndex) context.consumeIndex();

		try {
			List<Index> indexList = Index.getIndexList(folderIndex);
			for (Index index : indexList) {
				if (index.isFile()) {
					FileIndex fileIndex = (FileIndex) index;
					logger.debug("Initialize to change the protection key of meta document of index '"
							+ fileIndex.getName() + "'.");
					// create the process and make wrap it to make it asynchronous
					getParent().add(new AsyncComponent(buildProcess(fileIndex)));
				}
			}
		} catch (NoSessionException | NoPeerConnectionException e) {
			throw new ProcessExecutionException(e);
		}
	}

	private ProcessComponent buildProcess(FileIndex index) throws NoSessionException,
			NoPeerConnectionException {
		// create a new sub-process
		SequentialProcess sequential = new SequentialProcess();

		// each meta document gets own context
		MetaDocumentPKUpdateContext metaContext = new MetaDocumentPKUpdateContext(
				context.consumeOldProtectionKeys(), context.consumeNewProtectionKeys(),
				index.getFilePublicKey());
		sequential.add(new File2MetaFileComponent(index, metaContext, metaContext, networkManager));
		sequential.add(new ChangeProtectionKeyStep(metaContext, networkManager.getDataManager()));
		sequential.add(new InitializeChunkUpdateStep(metaContext, networkManager.getDataManager()));
		return sequential;
	}
}
