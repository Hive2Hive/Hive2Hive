package org.hive2hive.core.network.data.download.direct.process;

import java.security.PublicKey;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.network.data.PublicKeyManager;
import org.hive2hive.core.network.messages.IMessageManager;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.common.base.BaseDirectMessageProcessStep;

public class AskForChunkStep extends BaseDirectMessageProcessStep {

	private final PublicKeyManager keyManager;
	private final DownloadDirectContext context;

	public AskForChunkStep(DownloadDirectContext context, IMessageManager messageManager,
			PublicKeyManager keyManager) {
		super(messageManager);
		this.context = context;
		this.keyManager = keyManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		PublicKey publicKey;
		try {
			publicKey = keyManager.getPublicKey(context.getUserName());
		} catch (GetFailedException e) {
			throw new ProcessExecutionException("Cannot get public key of user " + context.getUserName());
		}

		// TODO ask for the file part (send a direct message)
	}

	@Override
	public void handleResponseMessage(ResponseMessage responseMessage) {
		// check the response, if file part is in it, ok
		// if not, delete the contacted peer from the list and ask next peer (re-run the process, so to say)
	}

}
