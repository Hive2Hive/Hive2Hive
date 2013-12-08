package org.hive2hive.core.test.process.notify;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.peers.PeerAddress;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.notify.INotificationMessageFactory;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.network.NetworkTestUtil;

/**
 * Simple message factory for testing. It counts the received messages by checking whether the
 * {@link TestDirectNotificationMessage} has put the ordered content (indicating that it arrived)
 * 
 * @author Nico
 * 
 */
public class CountingNotificationMessageFactory implements INotificationMessageFactory {

	private final NetworkManager sender;
	private final List<String> testContentKeys;
	private final H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());

	public CountingNotificationMessageFactory(NetworkManager sender) {
		this.sender = sender;
		testContentKeys = new ArrayList<String>();
	}

	@Override
	public BaseDirectMessage createNotificationMessage(PeerAddress receiver, String userId) {
		String contentKey = NetworkTestUtil.randomString();
		testContentKeys.add(contentKey);
		return new TestDirectNotificationMessage(receiver, sender.getNodeId(), contentKey, data);
	}

	public boolean allMsgsArrived() {
		return getSentMessageCount() == getArrivedMessageCount();
	}

	public int getSentMessageCount() {
		return testContentKeys.size();
	}

	public int getArrivedMessageCount() {
		int counter = 0;
		for (String contentKey : testContentKeys) {
			NetworkContent content = sender.getDataManager().getLocal(sender.getNodeId(), contentKey);
			if (content == null) {
				continue;
			}

			H2HTestData gotData = (H2HTestData) content;
			if (gotData.getTestString().equalsIgnoreCase(data.getTestString())) {
				counter++;
			}
		}

		return counter;
	}

}
