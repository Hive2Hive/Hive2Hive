package org.hive2hive.core.network.messages.request.callback;

import org.hive2hive.core.network.messages.direct.response.ResponseMessage;

public interface ICallBackHandler {
	public void handleReturnMessage(ResponseMessage asyncReturnMessage);
}
