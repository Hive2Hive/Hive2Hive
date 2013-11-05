package org.hive2hive.core.process;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureRemove;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;

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

	/**
	 * Call the DHT to remove a content. When it is done, it will call {@link ProcessStep.handleRemovalResult}
	 * 
	 * @param locationKey
	 * @param contentKey
	 */
	protected void remove(String locationKey, String contentKey) {
		if (getProcess().getState() == ProcessState.ROLLBACKING) {
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
