package org.hive2hive.core.network.data.download.direct.process;

import java.io.IOException;
import java.security.PublicKey;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseDirectMessageProcessStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskForChunkStep extends BaseDirectMessageProcessStep {

	private final static Logger logger = LoggerFactory.getLogger(AskForChunkStep.class);

	private final DownloadDirectContext context;
	private final PublicKeyManager keyManager;
	private final IFileConfiguration config;

	public AskForChunkStep(DownloadDirectContext context, IMessageManager messageManager,
			PublicKeyManager keyManager, IFileConfiguration config) {
		super(messageManager);
		this.context = context;
		this.keyManager = keyManager;
		this.config = config;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		if (context.getTask().isAborted()) {
			logger.warn("Not executing step because task is aborted");
			return;
		}

		PublicKey receiverPublicKey;
		try {
			receiverPublicKey = keyManager.getPublicKey(context.getUserName());
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("Cannot get public key of user " + context.getUserName());
		}

		MetaChunk metaChunk = context.getMetaChunk();
		RequestChunkMessage request = new RequestChunkMessage(context.getSelectedPeer(), context.getTask()
				.getFileKey(), metaChunk.getIndex(), config.getChunkSize(), metaChunk.getChunkHash());
		try {
			sendDirect(request, receiverPublicKey);
		} catch (SendFailedException e) {
			logger.error("Cannot send message to {}. Removing from the candidate list and ask other peer",
					context.getSelectedPeer());
			context.getTask().removeAddress(context.getSelectedPeer());
		}
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		// check the response, if chunk is in it, ok
		if (responseMessage.getContent() == null) {
			logger.error("Peer {} cannot send the chunk", context.getSelectedPeer());
			rerunProcess();
			return;
		}

		Chunk chunk = (Chunk) responseMessage.getContent();

		// verify the md5 hash
		byte[] respondedHash = EncryptionUtil.generateMD5Hash(chunk.getData());
		if (!H2HEncryptionUtil.compareMD5(respondedHash, context.getMetaChunk().getChunkHash())) {
			logger.error("Peer {} sent an invalid content", context.getSelectedPeer());
			rerunProcess();
			return;
		}

		// hash is ok, write it to the file
		try {
			FileUtils.writeByteArrayToFile(context.getTempDestination(), chunk.getData());
		} catch (IOException e) {
			context.getTask().abortDownload("Cannot write the chunk to the temporary file");
		}
	}

	/**
	 * Restarts the whole process, removing the currently selected peer from the candidate list
	 */
	private void rerunProcess() {
		logger.debug("Removing peer address {} from the candidate list", context.getSelectedPeer());
		context.getTask().removeAddress(context.getSelectedPeer());
		getParent().add(new SelectPeerForDownloadStep(context));
		getParent().add(new AskForChunkStep(context, messageManager, keyManager, config));
		logger.debug("Re-run the process: select another peer and ask him");
	}
}
