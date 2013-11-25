package org.hive2hive.core.process.notify;

import java.security.PublicKey;
import java.util.Map;

import org.hive2hive.core.network.messages.direct.BaseDirectMessage;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.context.ProcessContext;

public class NotifyPeersProcessContext extends ProcessContext {

	private final Map<String, BaseDirectMessage> notificationMessages;
	private Map<String, PublicKey> keys;

	public NotifyPeersProcessContext(Process process, Map<String, BaseDirectMessage> notificationMessages) {
		super(process);
		this.notificationMessages = notificationMessages;
	}

	public Map<String, BaseDirectMessage> getNotificationMessages() {
		return notificationMessages;
	}

	public void setUserPublicKeys(Map<String, PublicKey> keys) {
		this.keys = keys;
	}

	public Map<String, PublicKey> getUserPublicKeys() {
		return keys;
	}
}
