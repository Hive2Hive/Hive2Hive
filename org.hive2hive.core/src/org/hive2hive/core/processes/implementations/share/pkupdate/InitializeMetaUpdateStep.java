package org.hive2hive.core.processes.implementations.share.pkupdate;

import java.security.KeyPair;
import java.util.List;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.File2MetaFileComponent;
import org.hive2hive.core.processes.implementations.context.BasePKUpdateContext;
import org.hive2hive.core.processes.implementations.context.ShareProcessContext;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideMetaDocument;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideProtectionKeys;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.HybridEncryptedContent;

/**
 * Takes the shared folder and iteratively changes the protection keys of all meta documents
 * 
 * @author Nico
 * 
 */
public class InitializeMetaUpdateStep extends ProcessStep {

	private ShareProcessContext context;
	private NetworkManager networkManager;

	public InitializeMetaUpdateStep(ShareProcessContext context, NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		FolderIndex folderIndex = (FolderIndex) context.consumeIndex();

		List<Index> indexList = Index.getIndexList(folderIndex);
		try {
			for (Index index : indexList) {
				// create the process and make wrap it to make it asynchronous
				getParent().add(new AsyncComponent(buildProcess(index)));
			}
		} catch (NoSessionException | NoPeerConnectionException e) {
			throw new ProcessExecutionException(e);
		}
	}

	private ProcessComponent buildProcess(Index index) throws NoSessionException, NoPeerConnectionException {
		// create a new sub-process
		SequentialProcess sequential = new SequentialProcess();

		// each meta document gets own context
		MetaDocumentUpdateContext metaContext = new MetaDocumentUpdateContext(
				context.consumeOldProtectionKeys(), context.consumeNewProtectionKeys());
		sequential.add(new File2MetaFileComponent(index, metaContext, metaContext, networkManager));
		sequential.add(new ChangeProtectionKeyStep(metaContext, networkManager.getDataManager()));

		return sequential;
	}

	/**
	 * Inner class to provide the required context to update the meta document
	 */
	private class MetaDocumentUpdateContext extends BasePKUpdateContext implements IProvideProtectionKeys,
			IProvideMetaDocument {

		private MetaDocument metaDocument;
		private HybridEncryptedContent encryptedMetaDocument;

		public MetaDocumentUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys) {
			super(oldProtectionKeys, newProtectionKeys);
		}

		@Override
		public void provideProtectionKeys(KeyPair protectionKeys) {
			// ignore because this is the old protection key
		}

		@Override
		public void provideMetaDocument(MetaDocument metaDocument) {
			this.metaDocument = metaDocument;
		}

		@Override
		public void provideEncryptedMetaDocument(HybridEncryptedContent encryptedMetaDocument) {
			this.encryptedMetaDocument = encryptedMetaDocument;
		}

		@Override
		public NetworkContent getContent() {
			return encryptedMetaDocument;
		}

		@Override
		public String getLocationKey() {
			return H2HEncryptionUtil.key2String(metaDocument.getId());
		}

		@Override
		public String getContentKey() {
			return H2HConstants.META_DOCUMENT;
		}
	}
}
