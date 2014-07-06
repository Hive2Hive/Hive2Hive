package org.hive2hive.core.processes.notify;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.junit.Assert;

/**
 * Message to test whether a notification is received. As a verification, it puts a content on a given address
 * 
 * @author Nico
 */
public class TestDirectNotificationMessage extends BaseDirectMessage {

	private static final long serialVersionUID = 1873732460303405088L;
	private final String verificationLoc;
	private final String verificationContentKey;
	private final H2HTestData verificationData;

	public TestDirectNotificationMessage(PeerAddress targetAddress, String verificationLoc,
			String verificationContentKey, H2HTestData verificationData) {
		super(targetAddress);
		this.verificationLoc = verificationLoc;
		this.verificationContentKey = verificationContentKey;
		this.verificationData = verificationData;
	}

	@Override
	public void run() {
		// put for verification
		try {
			networkManager
					.getDataManager()
					.putUnblocked(
							new Parameters().setLocationKey(verificationLoc)
									.setContentKey(verificationContentKey).setData(verificationData))
					.awaitUninterruptibly();
		} catch (NoPeerConnectionException e) {
			Assert.fail();
		}
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}

}
