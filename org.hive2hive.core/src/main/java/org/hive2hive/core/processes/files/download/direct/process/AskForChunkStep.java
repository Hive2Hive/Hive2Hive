package org.hive2hive.core.processes.files.download.direct.process;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.SendFailedException;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.common.base.BaseDirectMessageProcessStep;
import org.hive2hive.core.security.HashUtil;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskForChunkStep extends BaseDirectMessageProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(AskForChunkStep.class);

	private final DownloadDirectContext context;
	private final PublicKeyManager keyManager;
	private final IFileConfiguration config;
	private final CountDownLatch responseLatch;

	public AskForChunkStep(DownloadDirectContext context, IMessageManager messageManager, PublicKeyManager keyManager,
			IFileConfiguration config) {
		super(messageManager);
		this.context = context;
		this.keyManager = keyManager;
		this.config = config;
		this.responseLatch = new CountDownLatch(1);
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
		RequestChunkMessage request = new RequestChunkMessage(context.getSelectedPeer(), context.getTask().getFileKey(),
				metaChunk.getIndex(), config.getChunkSize(), metaChunk.getChunkHash());
		try {
			logger.debug("Requesting chunk {} from peer {}", metaChunk.getIndex(), context.getSelectedPeer());
			sendDirect(request, receiverPublicKey);
			responseLatch.await(H2HConstants.DIRECT_DOWNLOAD_AWAIT_MS, TimeUnit.MILLISECONDS);
		} catch (SendFailedException e) {
			logger.error("Cannot send message to {}", context.getSelectedPeer(), e);
			rerunProcess(true);
		} catch (InterruptedException e) {
			logger.warn("Cannot wait until the peer {} responded", context.getSelectedPeer());
			rerunProcess(false);
		}
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		MetaChunk metaChunk = context.getMetaChunk();

		// check the response
		if (responseMessage.getContent() == null) {
			logger.error("Peer {} did not send the chunk {}", context.getSelectedPeer(), metaChunk.getIndex());
			rerunProcess(true);
			return;
		}

		ChunkMessageResponse response = (ChunkMessageResponse) responseMessage.getContent();
		switch (response.getAnswerType()) {
			case DECLINED:
				logger.error("Peer {} declined to send chunk {}", context.getSelectedPeer(), metaChunk.getIndex());
				rerunProcess(true);
				break;
			case ASK_LATER:
				logger.error("Peer {} is alive but cannot send chunk {} at the moment", context.getSelectedPeer(),
						metaChunk.getIndex());
				sleepRandomTime();
				rerunProcess(false);
				break;
			case OK:
				verifyAndWriteChunk(metaChunk, response.getChunk());
				break;
			default:
				break;
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
		// verify the md5 hash
		byte[] respondedHash = HashUtil.hash(chunk.getData());
		if (HashUtil.compare(respondedHash, metaChunk.getChunkHash())) {
			logger.debug("Peer {} sent a valid content for chunk {}. MD5 verified.", context.getSelectedPeer(),
					metaChunk.getIndex());
		} else {
			logger.error("Peer {} sent an invalid content for chunk {}.", context.getSelectedPeer(), metaChunk.getIndex());
			rerunProcess(true);
			return;
		}

		// hash is ok, write it to the file
		try {
			FileUtils.writeByteArrayToFile(context.getTempDestination(), chunk.getData());
			logger.debug("Wrote chunk {} to temporary file {}", context.getMetaChunk().getIndex(),
					context.getTempDestination());

			// finalize the sub-process
			context.getTask().setDownloaded(context.getMetaChunk().getIndex(), context.getTempDestination());
		} catch (IOException e) {
			context.getTask().abortDownload("Cannot write the chunk to the temporary file. Reason: " + e.getMessage());
		} finally {
			// release the lock such that the process can finish correctly
			responseLatch.countDown();
		}
	}

	/**
	 * Restarts the whole process, removing the currently selected peer from the candidate list
	 */
	private void rerunProcess(boolean removeLastSelection) {
		if (removeLastSelection) {
			logger.debug("Removing peer address {} from the candidate list", context.getSelectedPeer());
			// remove invalid peer
			context.getTask().removeAddress(context.getSelectedPeer());
		}

		// select another peer
		logger.debug("Re-run the process: select another peer and ask him for chunk {}", context.getMetaChunk().getIndex());
		getParent().add(new SelectPeerForDownloadStep(context));
		getParent().add(new AskForChunkStep(context, messageManager, keyManager, config));

		// continue with the currently initialized process steps
		responseLatch.countDown();
	}
}
