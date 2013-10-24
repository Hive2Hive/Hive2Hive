package org.hive2hive.core.process;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDHT;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkData;

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
public class PutVerificationListener extends BaseFutureAdapter<FutureDHT> {

	private final NetworkManager networkManager;
	private final String locationKey;
	private final String contentKey;
	private final NetworkData expectedData;
	private final List<BaseFutureAdapter<FutureDHT>> listeners;

	public PutVerificationListener(NetworkManager networkManager, String locationKey, String contentKey,
			NetworkData expectedData) {
		this.networkManager = networkManager;
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.expectedData = expectedData;
		listeners = new ArrayList<BaseFutureAdapter<FutureDHT>>();
	}

	@Override
	public void operationComplete(FutureDHT future) throws Exception {
		// store the future for notifying the listeners later with this
		final FutureDHT putFuture = future;
		FutureDHT getFuture = networkManager.getGlobal(locationKey, contentKey);
		getFuture.addListener(new BaseFutureAdapter<FutureDHT>() {

			@Override
			public void operationComplete(FutureDHT future) throws Exception {
				// TODO: verify with expected data and the timestamps
				notifyListeners(putFuture);
			}
		});
	}

	private void notifyListeners(FutureDHT future) throws Exception {
		for (BaseFutureAdapter<FutureDHT> listener : listeners) {
			listener.operationComplete(future);
		}
	}

	/**
	 * Adds a listener which are notified after the verification
	 * 
	 * @param listener
	 */
	public void addListener(BaseFutureAdapter<FutureDHT> listener) {
		listeners.add(listener);
	}

}
