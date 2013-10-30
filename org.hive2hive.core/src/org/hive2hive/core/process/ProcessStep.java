package org.hive2hive.core.process;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.BaseRequestMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;

/**
 * One step of a complete workflow. This step calls the next step after finishing
 * 
 * @author Nico
 * 
 */
public abstract class ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(ProcessStep.class);
	private Process process;

	public void setProcess(Process aProcess) {
		process = aProcess;
	}

	protected Process getProcess() {
		return process;
	}

	/* shortcut */
	protected NetworkManager getNetworkManager() {
		return process.getNetworkManager();
	}

	/**
	 * Called by the containing process to tell this step to start with its work.
	 */
	public abstract void start();

	/**
	 * Tells this step to undo any work it did previously. If this step changed anything in the network it
	 * needs to be revoked completely. After the execution of this method the global state of the network
	 * needs to be the same as if this step never existed.
	 */
	public abstract void rollBack();

	/**
	 * An optional method which my be implemented blank if not needed.</br>
	 * If this step needs to send out {@link IRequestMessage}(s), this method will be called once the
	 * {@link ResponseMessage} arrived at this node. To send a {@link IRequestMessage}, a step needs to use
	 * {@link ProcessStep#send(org.hive2hive.core.messages.request.BaseRequestMessage)}.</br></br>
	 * <b>Advice:</b></br>
	 * Although it is possible for a step to send out multiple {@link IRequestMessage}s this should be avoided
	 * if possible. We recommend to use a separate step for each request. This eases the reading and
	 * encapsulates one action in one step only.
	 * 
	 * @param asyncReturnMessage the {@link Responsemessage} containing the result of the request.
	 */
	protected abstract void handleMessageReply(ResponseMessage asyncReturnMessage);

	/**
	 * An optional method which my be implemented blank if not needed.</br>
	 * If this step needs to put something into the DHT, this method will be called once the {@link FutureDHT}
	 * is done at this node.</br></br>
	 * <b>Advice:</b></br>
	 * Although it is possible for a step to do multiple puts, this should be avoided
	 * if possible. We recommend to use a separate step for each request. This eases the reading and
	 * encapsulates one action in one step only.
	 * 
	 * @param future the {@link FutureDHT} containing the result of the request.
	 */
	protected abstract void handlePutResult(FuturePut future);

	/**
	 * An optional method which my be implemented blank if not needed.</br>
	 * If this step needs to get something from the DHT, this method will be called once the {@link FutureDHT}
	 * is done at this node.</br></br>
	 * <b>Advice:</b></br>
	 * Although it is possible for a step to do multiple gets, this should be avoided
	 * if possible. We recommend to use a separate step for each request. This eases the reading and
	 * encapsulates one action in one step only.
	 * 
	 * @param future the {@link FutureDHT} containing the result of the request.
	 */
	protected abstract void handleGetResult(FutureGet future);

	/**
	 * An optional method which my be implemented blank if not needed.</br>
	 * If this step needs to get something from the DHT, this method will be called once the {@link FutureDHT}
	 * is done at this node.</br></br>
	 * <b>Advice:</b></br>
	 * Although it is possible for a step to do multiple gets, this should be avoided
	 * if possible. We recommend to use a separate step for each request. This eases the reading and
	 * encapsulates one action in one step only.
	 * 
	 * @param future the {@link FutureDHT} containing the result of the request.
	 */
	protected abstract void handleRemovalResult(FutureRemove future);

	protected void send(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			BaseRequestMessage requestMessage = (BaseRequestMessage) message;
			requestMessage.setCallBackHandler(new ICallBackHandler() {
				@Override
				public void handleReturnMessage(ResponseMessage asyncReturnMessage) {
					handleMessageReply(asyncReturnMessage);
				}
			});
		}
		getNetworkManager().send(message);
	}

	/**
	 * Make a put to the DHT. This is a non-blocking call; when it is done, it will call
	 * {@link ProcessStep.handlePutResult}
	 * 
	 * @param locationKey
	 * @param contentKey
	 * @param data
	 */
	protected void put(final String locationKey, final String contentKey, NetworkContent data) {
		if (getProcess().getState() == ProcessState.ROLLBACK) {
			rollbackPut(locationKey, contentKey, data);
			return;
		}

		// TODO: Verify with old version of the data
		FuturePut putFuture = getNetworkManager().putGlobal(locationKey, contentKey, data);
		PutVerificationListener verificationListener = new PutVerificationListener(getNetworkManager(), this,
				locationKey, contentKey, data);
		putFuture.addListener(verificationListener);
	}

	/**
	 * Put specialized for rollback (e.g. when restoring original data). Exceptions and callbacks are
	 * suppressed (nobody cares about rollbacks of rollbacks)
	 * 
	 * @param locationKey
	 * @param contentKey
	 * @param data
	 */
	private void rollbackPut(String locationKey, String contentKey, NetworkContent data) {
		FuturePut rollbackFuture = getNetworkManager().putGlobal(locationKey, contentKey, data);
		rollbackFuture.addListener(new BaseFutureAdapter<FuturePut>() {
			@Override
			public void operationComplete(FuturePut future) throws Exception {
				if (future.isSuccess()) {
					logger.debug("Rollback: Restored the old content");
				} else {
					logger.error("Rollback: Could not restore the old content");
				}
			}
		});
	}

	/**
	 * Make a get to the DHT. This is a non-blocking call; when it is done, it will call
	 * {@link ProcessStep.handleGetResult}
	 * 
	 * @param locationKey
	 * @param contentKey
	 */
	protected void get(String locationKey, String contentKey) {
		FutureGet getFuture = getNetworkManager().getGlobal(locationKey, contentKey);
		getFuture.addListener(new BaseFutureAdapter<FutureGet>() {
			@Override
			public void operationComplete(FutureGet future) throws Exception {
				handleGetResult(future);
			}
		});
	}

	/**
	 * Call the DHT to remove a content. When it is done, it will call {@link ProcessStep.handleRemovalResult}
	 * 
	 * @param locationKey
	 * @param contentKey
	 */
	protected void remove(String locationKey, String contentKey) {
		if (getProcess().getState() == ProcessState.ROLLBACK) {
			rollbackRemove(locationKey, contentKey);
			return;
		}

		FutureRemove removalFuture = getNetworkManager().remove(locationKey, contentKey);
		removalFuture.addListener(new BaseFutureAdapter<FutureRemove>() {
			@Override
			public void operationComplete(FutureRemove future) throws Exception {
				handleRemovalResult(future);
			}
		});
	}

	/**
	 * Remove specialized for rollback (e.g. when deleting added data). Exceptions and callbacks are
	 * suppressed (nobody cares about rollbacks of rollbacks)
	 * 
	 * @param locationKey
	 * @param contentKey
	 */
	private void rollbackRemove(String locationKey, String contentKey) {
		FutureRemove rollbackFuture = getNetworkManager().remove(locationKey, contentKey);
		rollbackFuture.addListener(new BaseFutureAdapter<FutureRemove>() {
			@Override
			public void operationComplete(FutureRemove future) throws Exception {
				if (future.isSuccess()) {
					logger.debug("Rollback: Removed new content");
				} else {
					logger.error("Rollback: Could not delete the newly put content");
				}
			}
		});
	}
}
