package org.hive2hive.core.process;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;

import org.apache.log4j.Logger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;

/**
 * Verifies a put by checking if all replicas accepted the content. If not, the version is compared and the
 * newest version is taken. Old deprecated versions are cleaned up.
 * After the verification, the listeners on this object are called.
 * 
 * This class is needed because there could emerge inconsistent states when using replication
 * 
 * @author Nico
 * 
 */
public class PutVerificationListener extends BaseFutureAdapter<FuturePut> {

	private final static Logger logger = H2HLoggerFactory.getLogger(PutVerificationListener.class);

	private final NetworkManager networkManager;
	private final String locationKey;
	private final String contentKey;
	private final NetworkContent expectedData;
	private final List<BaseFutureAdapter<FuturePut>> listeners;

	public PutVerificationListener(NetworkManager networkManager, String locationKey, String contentKey,
			NetworkContent expectedData) {
		this.networkManager = networkManager;
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.expectedData = expectedData;
		listeners = new ArrayList<BaseFutureAdapter<FuturePut>>();
	}

	@Override
	public void operationComplete(FuturePut future) throws Exception {
		// store the future for notifying the listeners later with this
		logger.debug("Start verification of put(" + locationKey + ", " + contentKey + ")");
		final FuturePut putFuture = future;
		// TODO
		// get the raw keys from the putFuture
		// if raw keys have empty collection --> did not save because of verification (previous version does
		// not match)

		// wait some seconds

		FutureGet getFuture = networkManager.getGlobal(locationKey, contentKey);
		getFuture.addListener(new BaseFutureAdapter<FutureGet>() {

			@Override
			public void operationComplete(FutureGet future) throws Exception {
				// TODO

				// get history object and verify if my key is contained
				// if newest in history --> my object is most recent one
				// if not newest in history --> already newer version, but that's based on my version
				// if not in list --> fork happened and I'm not part of the "right" branch. --> Rollback to
				// base on newest version --> possibly throw an exception or repeat the put based on the most
				// recent version
				notifyListeners(putFuture);
			}
		});
	}

	private void notifyListeners(FuturePut future) throws Exception {
		for (BaseFutureAdapter<FuturePut> listener : listeners) {
			listener.operationComplete(future);
		}
	}

	/**
	 * Adds a listener which are notified after the verification
	 * 
	 * @param listener
	 */
	public void addListener(BaseFutureAdapter<FuturePut> listener) {
		listeners.add(listener);
	}

}
