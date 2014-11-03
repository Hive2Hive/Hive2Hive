package org.hive2hive.core.processes.files.delete;

import java.nio.file.Path;
import java.nio.file.Paths;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.events.framework.interfaces.IFileEventGenerator;
import org.hive2hive.core.events.implementations.FileDeleteEvent;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteNotificationMessage extends BaseDirectMessage implements IFileEventGenerator {

	private static final long serialVersionUID = 5518489264065301800L;

	private static final Logger logger = LoggerFactory.getLogger(DeleteNotificationMessage.class);

	private final String relativeFilePath;
	private final boolean isFile;

	public DeleteNotificationMessage(PeerAddress targetAddress, String relativeFilePath, boolean isFile) {
		super(targetAddress);
		this.relativeFilePath = relativeFilePath;
		this.isFile = isFile;
	}

	@Override
	public void run() {
		logger.debug("Delete file notification message received.");

		H2HSession session;
		try {
			session = networkManager.getSession();
		} catch (NoSessionException e) {
			logger.error("No user seems to be logged in.");
			return;
		}

		// trigger event
		Path deletedFilePath = Paths.get(session.getRoot().toString(), relativeFilePath);
		getEventBus().publish(new FileDeleteEvent(deletedFilePath, isFile));
	}
}
