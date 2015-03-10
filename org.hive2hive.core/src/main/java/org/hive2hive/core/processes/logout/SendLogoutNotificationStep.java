package org.hive2hive.core.processes.logout;

import java.security.PublicKey;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.messages.MessageManager;
import org.hive2hive.core.network.messages.direct.LogoutNotificationMessage;
import org.hive2hive.core.processes.context.interfaces.LogoutProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendLogoutNotificationStep extends ProcessStep<Void> {

	private static final Logger logger = LoggerFactory.getLogger(SendLogoutNotificationStep.class);

	private final LogoutProcessContext context;
	private final NetworkManager networkManager;

	public SendLogoutNotificationStep(NetworkManager networkManager, LogoutProcessContext context) {
		this.networkManager = networkManager;
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		try {
			final PublicKey publicKey = networkManager.getSession().getKeyManager().getOwnPublicKey();
			final MessageManager messageManager = networkManager.getMessageManager();
			final CountDownLatch latch = new CountDownLatch(context.consumeNotificationRecipients().size());
			for (final PeerAddress client : context.consumeNotificationRecipients()) {
				// notify them all in multiple threads
				new Thread(new Runnable() {
					@Override
					public void run() {
						messageManager.sendDirect(new LogoutNotificationMessage(client), publicKey);
						logger.trace("Sent the logout notification message to client {}", client);
						latch.countDown();
					}
				}, "Logout notification " + client.peerId()).start();
			}

			boolean success = latch.await(H2HConstants.CONTACT_PEERS_AWAIT_MS, TimeUnit.MILLISECONDS);
			if (!success) {
				logger.warn("Not all logout notification messages could be sent... Ignoring the rest");
			}
		} catch (NoPeerConnectionException | NoSessionException | InterruptedException e) {
			logger.warn("Cannot send the logout notification message", e);
		}
		return null;
	}
}
