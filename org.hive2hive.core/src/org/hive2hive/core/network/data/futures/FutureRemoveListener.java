package org.hive2hive.core.network.data.futures;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDigest;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.builder.DigestBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.listener.IRemoveListener;

/**
 * A future listener for a remove. After the operation completed the listener verifies with a get digest if
 * all data has been deleted. If not, the listener retries the remove (see {@link H2HConstants#REMOVE_RETRIES}
 * ). In both cases the given {@link IRemoveListener} listener gets notified. </br></br>
 * For further details see {@link DataManager#remove(String, String, Number160, IRemoveListener)}
 * 
 * @author Seppi
 */
public class FutureRemoveListener extends BaseFutureAdapter<FutureRemove> {

	private final static Logger logger = H2HLoggerFactory.getLogger(FutureRemoveListener.class);

	// used to count remove retries
	private int removeTries = 0;

	protected final Number160 locationKey;
	protected final Number160 domainKey;
	protected final Number160 contentKey;
	protected final Number160 versionKey;
	protected final IRemoveListener listener;
	protected final DataManager dataManager;

	public FutureRemoveListener(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			IRemoveListener listener, DataManager dataManager) {
		this.locationKey = locationKey;
		this.domainKey = domainKey;
		this.contentKey = contentKey;
		this.versionKey = null;
		this.listener = listener;
		this.dataManager = dataManager;
	}

	public FutureRemoveListener(Number160 locationKey, Number160 domainKey, Number160 contentKey,
			Number160 versionKey, IRemoveListener listener, DataManager dataManager) {
		this.locationKey = locationKey;
		this.domainKey = domainKey;
		this.contentKey = contentKey;
		this.versionKey = versionKey;
		this.listener = listener;
		this.dataManager = dataManager;
	}

	@Override
	public void operationComplete(FutureRemove future) throws Exception {
		logger.debug(String.format("Start verification of remove."
				+ " location key = '%s' domain key = '%s' content key = '%s' version key = '%s'",
				locationKey, domainKey, contentKey, versionKey));
		// get data to verify if everything went correct
		DigestBuilder digestBuilder = dataManager.getDigest(locationKey);
		if (versionKey != null) {
			digestBuilder.setDomainKey(domainKey).setContentKey(contentKey).setVersionKey(versionKey);
		} else {
			digestBuilder.from(new Number640(locationKey, domainKey, contentKey, Number160.ZERO)).to(
					new Number640(locationKey, domainKey, contentKey, Number160.MAX_VALUE));
		}
		FutureDigest digestFuture = digestBuilder.start();
		digestFuture.addListener(new BaseFutureAdapter<FutureDigest>() {
			@Override
			public void operationComplete(FutureDigest future) throws Exception {
				if (future.getDigest() == null) {
					retryRemove();
				} else if (!future.getDigest().getKeyDigest().isEmpty()) {
					retryRemove();
				} else {
					logger.debug(String.format("Verification for remove completed."
							+ " location key = '%s' domain key = '%s' content key = '%s' versionKey = '%s'",
							locationKey, domainKey, contentKey, versionKey));
					if (listener != null)
						listener.onRemoveSuccess();
				}
			}
		});
	}

	private void retryRemove() {
		if (removeTries++ < H2HConstants.REMOVE_RETRIES) {
			logger.warn(String.format("Remove verification failed. Data is not null. Try #%s."
					+ " location key = '%s' domain key = '%s' content key = '%s' versionKey = '%s'",
					removeTries, locationKey, domainKey, contentKey, versionKey));
			if (versionKey == null) {
				dataManager.remove(locationKey, domainKey, contentKey).addListener(this);
			} else {
				dataManager.remove(locationKey, domainKey, contentKey, versionKey).addListener(this);
			}
		} else {
			logger.error(String.format("Remove verification failed. Data is not null after %s tries."
					+ " location key = '%s' domain key = '%s' content key = '%s'", removeTries - 1,
					locationKey, domainKey, contentKey, versionKey));
			if (listener != null)
				listener.onRemoveFailure();
		}
	}
}
