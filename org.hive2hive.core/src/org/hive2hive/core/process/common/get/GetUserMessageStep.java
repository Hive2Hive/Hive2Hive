package org.hive2hive.core.process.common.get;

import java.security.PublicKey;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.process.ProcessStep;

/**
 * This step is only important for a master client that has to handle all the buffered user messages.
 * 
 * @author Christian
 * 
 */
public class GetUserMessageStep extends BaseGetProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetUserMessageStep.class);

	private final String userId;
	private final ProcessStep nextStep;
	private final PublicKey publicKey;
	private BaseMessage userMessage;

	public GetUserMessageStep(String userId, ProcessStep nextStep, PublicKey publicKey) {
		this.userId = userId;
		this.nextStep = nextStep;
		this.publicKey = publicKey;
	}

	@Override
	public void handleGetResult(NetworkContent content) {
		if (content == null) {
			logger.debug("Did not find the user message.");
		} else {

			// TODO @Seppi: verification is now done right within the UserProfileTask itself
//			// verify the user message
//			UserMessageContainer container = (UserMessageContainer) content;
//			boolean isVerified = false;
//			try {
//				isVerified = EncryptionUtil.verify(container.getMessageBytes(), container.getMessageSignature(), publicKey);
//			} catch (InvalidKeyException | SignatureException e) {
//				logger.error("Exception while verifying user message: ", e);
//				e.printStackTrace();
//			}
//			
//			if (isVerified) {
//				userMessage = (BaseMessage) EncryptionUtil.deserializeObject(container.getMessageBytes());
//			} else {
//				logger.warn("User Message could not be verified.");
//			}
			
			// continue with next step
			getProcess().setNextStep(nextStep);
		}

		// TODO check whether this step setting is necessary here. Alternative: only parent-process knows next
		// step and this GetUserMessageQueueStep calls getProcess().stop() and initiates a rollback
		getProcess().setNextStep(nextStep);
	}

	public BaseMessage getUserMessage() {
		return userMessage;
	}

	@Override
	public void start() {
		// TODO correct contentKey
		get(userId, H2HConstants.USER_MESSAGE_QUEUE_KEY);
	}
}