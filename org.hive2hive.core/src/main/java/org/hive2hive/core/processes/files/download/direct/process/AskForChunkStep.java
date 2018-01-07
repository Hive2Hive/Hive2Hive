package org.hive2hive.core.processes.files.download.direct.process;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.common.base.BaseMessageProcessStep;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskForChunkStep extends BaseMessageProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(AskForChunkStep.class);

	private final DownloadDirectContext context;
	private final IFileConfiguration config;

	private ProcessExecutionException responseException;

	public AskForChunkStep(DownloadDirectContext context, IMessageManager messageManager, IFileConfiguration config) {
		super(messageManager);
		this.setName(getClass().getName());
		this.context = context;
		this.config = config;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		if (context.getTask().isAborted()) {
			logger.warn("Not executing step because task is aborted.");
			return null;
		}

		PublicKey receiverPublicKey;
		try {
			receiverPublicKey = context.getTask().getKeyManager().getPublicKey(context.getUserName());
		} catch (GetFailedException ex) {
			throw new ProcessExecutionException(this, String.format("Cannot get public key of user '%s'.",
					context.getUserName()));
		}

		MetaChunk metaChunk = context.getMetaChunk();
		RequestChunkMessage request = new RequestChunkMessage(context.getSelectedPeer(), context.getTask().getFileKey(),
				metaChunk.getIndex(), config.getChunkSize(), metaChunk.getChunkHash());
		try {
			logger.debug("Requesting chunk {} from peer {}", metaChunk.getIndex(), context.getSelectedPeer());
			send(request, receiverPublicKey);
		} catch (SendFailedException e) {
			logger.error("Cannot send message to {}", context.getSelectedPeer(), e);
			removeLocation();
			throw new ProcessExecutionException(this, e, "Message cannot be sent");
		}

		if (responseException != null) {
			// an exception in the response message occurred
			throw responseException;
		}

		return null;
	}

	@Override
	public void handleResponse(ResponseMessage responseMessage) {
		MetaChunk metaChunk = context.getMetaChunk();

		// check the response
		if (responseMessage.getContent() == null) {
			logger.error("Peer {} did not send the chunk {}", context.getSelectedPeer(), metaChunk.getIndex());
			responseException = new ProcessExecutionException(this, "Empty response message received");
			removeLocation();
			return;
		}

		ChunkMessageResponse response = (ChunkMessageResponse) responseMessage.getContent();
		switch (response.getAnswerType()) {
			case DECLINED:
				logger.error("Peer {} declined to send chunk {}", context.getSelectedPeer(), metaChunk.getIndex());
				responseException = new ProcessExecutionException(this, "Peer declined to send the chunk");
				removeLocation();
				break;
			case ASK_LATER:
				logger.error("Peer {} is alive but cannot send chunk {} at the moment", context.getSelectedPeer(),
						metaChunk.getIndex());
				responseException = new ProcessExecutionException(this, "Chunk could not be provided. Probably ask later");
				sleepRandomTime();
				break;
			case OK:
				verifyAndWriteChunk(metaChunk, response.getChunk());
				break;
			default:
				logger.error("Invaid response type when downloading chunk {}: {}", context.getMetaChunk().getIndex(),
						response.getAnswerType());
				responseException = new ProcessExecutionException(this, "Invalid response received");
		}
	}

	private void sleepRandomTime() {
		try {
			int sleep = new Random().nextInt(H2HConstants.DIRECT_DOWNLOAD_RETRY_MS);
			logger.debug("Sleep {} ms before retrying to download", sleep);
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			logger.warn("Cannot sleep before retrying");
		}
	}

	private void verifyAndWriteChunk(MetaChunk metaChunk, Chunk chunk) {
		// verify the hash
		byte[] respondedHash = HashUtil.hash(chunk.getData());
		if (HashUtil.compare(respondedHash, metaChunk.getChunkHash())) {
			logger.debug("Peer {} sent a valid content for chunk {}. Hash verified.", context.getSelectedPeer(),
					metaChunk.getIndex());
		} else {
			logger.error("Peer {} sent an invalid content for chunk {}.", context.getSelectedPeer(), metaChunk.getIndex());
			responseException = new ProcessExecutionException(this, "Invalid chunk received");
			removeLocation();
			return;
		}

		// hash is ok, write it to the file
		try {
			FileUtils.writeByteArrayToFile(context.getTempDestination(), chunk.getData());
			logger.debug("Wrote chunk {} to temporary file {}", context.getMetaChunk().getIndex(),
					context.getTempDestination());

			// finalize the sub-process
			context.getTask().markDownloaded(context.getMetaChunk().getIndex(), context.getTempDestination());
		} catch (IOException e) {
			context.getTask().abortDownload("Cannot write the chunk to the temporary file. Reason: " + e.getMessage());
		}
	}

	/**
	 * Restarts the whole process, removing the currently selected peer from the candidate list
	 */
	private void removeLocation() {
		logger.debug("Removing peer address {} from the candidate list", context.getSelectedPeer());
		// remove invalid peer
		context.getTask().removeAddress(context.getSelectedPeer());
	}
}
