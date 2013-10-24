package org.hive2hive.core.process;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDHT;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkData;
import org.hive2hive.core.network.messages.BaseMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.network.messages.request.IRequestMessage;
import org.hive2hive.core.network.messages.request.callback.ICallBackHandler;

/**
 * One step of a complete workflow. This step calls the next step after finishing
 * 
 * @author Nendor, Nico
 * 
 */
public abstract class ProcessStep {

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
	 * If this step needs to put or get something from the DHT, this method will be called once the
	 * {@link FutureDHT} is done at this node.</br></br>
	 * <b>Advice:</b></br>
	 * Although it is possible for a step to do multiple puts or gets, this should be avoided
	 * if possible. We recommend to use a separate step for each request. This eases the reading and
	 * encapsulates one action in one step only.
	 * 
	 * @param future the {@link FutureDHT} containing the result of the request.
	 */
	protected abstract void handlePutGetResult(FutureDHT future);

	protected void send(BaseMessage message) {
		if (message instanceof IRequestMessage) {
			IRequestMessage requestMessage = (IRequestMessage) message;
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
	 * {@link ProcessStep.handlePutGetResult}
	 * 
	 * @param locationKey
	 * @param contentKey
	 * @param wrapper the data
	 */
	protected void put(String locationKey, String contentKey, NetworkData wrapper) {
		FutureDHT putFuture = getNetworkManager().putGlobal(locationKey, contentKey, wrapper);
		putFuture.addListener(new BaseFutureAdapter<FutureDHT>() {
			@Override
			public void operationComplete(FutureDHT future) throws Exception {
				handlePutGetResult(future);
			}
		});
	}

	/**
	 * Make a get to the DHT. This is a non-blocking call; when it is done, it will call
	 * {@link ProcessStep.handlePutGetResult}
	 * 
	 * @param locationKey
	 * @param contentKey
	 */
	protected void get(String locationKey, String contentKey) {
		FutureDHT getFuture = getNetworkManager().getGlobal(locationKey, contentKey);
		getFuture.addListener(new BaseFutureAdapter<FutureDHT>() {
			@Override
			public void operationComplete(FutureDHT future) throws Exception {
				handlePutGetResult(future);
			}
		});
	}
}
