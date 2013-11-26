package org.hive2hive.core.network.data;

import org.hive2hive.core.network.messages.usermessages.UserMessageContainer;

public interface IGetUserMessageListener {

	public void handleGetResult(UserMessageContainer userMessageContainer);

}
