package org.hive2hive.core.test.process.notify;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.test.H2HTestData;

/**
 * Message to test whether a notification is received. As a verification, it puts a content on a given address
 * 
 * @author Nico
 * 
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
		networkManager.getDataManager().put(Number160.createHash(verificationLoc),
				H2HConstants.TOMP2P_DEFAULT_KEY, Number160.createHash(verificationContentKey),
				verificationData, null).awaitUninterruptibly();
	}

	@Override
	public AcceptanceReply accept() {
		return AcceptanceReply.OK;
	}
	
}
