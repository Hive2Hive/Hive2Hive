package org.hive2hive.core.processes.files.download.direct;

import java.io.File;

import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.processes.files.download.direct.process.AskForChunkStep;
import org.hive2hive.core.processes.files.download.direct.process.DownloadDirectContext;
import org.hive2hive.core.processes.files.download.direct.process.SelectPeerForDownloadStep;
import org.hive2hive.processframework.concretes.SequentialProcess;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a chunk from the DHT and stores it into a temporary file
 * 
 * @author Nico
 * 
 */
public class DownloadChunkRunnableDirect implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DownloadChunkRunnableDirect.class);

	private final DownloadTaskDirect task;
	private final MetaChunk metaChunk;
	private final File tempDestination;
	private final IMessageManager messageManager;
	private final PublicKeyManager keyManager;
	private final IFileConfiguration config;

	public DownloadChunkRunnableDirect(DownloadTaskDirect task, MetaChunk chunk, IMessageManager messageManager,
			PublicKeyManager keyManager, IFileConfiguration config) {
		this.task = task;
		this.metaChunk = chunk;
		this.keyManager = keyManager;
		this.messageManager = messageManager;
		this.config = config;

		// create temporary file
		this.tempDestination = new File(task.getTempDirectory(), task.getDestinationName() + "-" + chunk.getIndex());
	}

	@Override
	public void run() {
		if (task.isAborted()) {
			logger.warn("Abort scheduled download of chunk {} of file {}", metaChunk.getIndex(), task.getDestinationName());
			return;
		} else if (Thread.currentThread().isInterrupted()) {
			logger.warn("Not terminate the download because thread is interrupted");
			return;
		}

		DownloadDirectContext context = new DownloadDirectContext(task, metaChunk, tempDestination);
		SequentialProcess process = new SequentialProcess();
		process.add(new SelectPeerForDownloadStep(context));
		process.add(new AskForChunkStep(context, messageManager, keyManager, config));

		try {
			process.start().await();
		} catch (InvalidProcessStateException | InterruptedException e) {
			task.abortDownload(e.getMessage());
		}
	}
}
