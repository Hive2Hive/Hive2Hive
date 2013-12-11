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

	protected final String locationKey;
	protected final String contentKey;
	protected final Number160 versionKey;
	protected final IRemoveListener listener;
	protected final DataManager dataManager;

	public FutureRemoveListener(String locationKey, String contentKey, Number160 versionKey,
			IRemoveListener listener, DataManager dataManager) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.versionKey = versionKey;
		this.listener = listener;
		this.dataManager = dataManager;
	}

	public FutureRemoveListener(String locationKey, String contentKey, IRemoveListener listener,
			DataManager dataManager) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.versionKey = null;
		this.listener = listener;
		this.dataManager = dataManager;
	}

	@Override
	public void operationComplete(FutureRemove future) throws Exception {
		logger.debug(String.format(
				"Start verification of remove. locationKey = '%s' contentKey = '%s' versionKey = '%s'",
				locationKey, contentKey, versionKey));
		// get data to verify if everything went correct
		DigestBuilder digestBuilder = dataManager.getDigest(locationKey);
		if (versionKey != null) {
			digestBuilder.setDomainKey(H2HConstants.TOMP2P_DEFAULT_KEY)
					.setContentKey(Number160.createHash(contentKey)).setVersionKey(versionKey);
		} else {
			digestBuilder.from(
					new Number640(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
							Number160.createHash(contentKey), Number160.ZERO)).to(
					new Number640(Number160.createHash(locationKey), H2HConstants.TOMP2P_DEFAULT_KEY,
							Number160.createHash(contentKey), Number160.MAX_VALUE));
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
					logger.debug(String
							.format("Verification for remove completed. location key = '%s' content key = '%s' versionKey = '%s'",
									locationKey, contentKey, versionKey));
					if (listener != null)
						listener.onRemoveSuccess();
				}
			}
		});
	}

	private void retryRemove() {
		if (removeTries++ < H2HConstants.REMOVE_RETRIES) {
			logger.warn(String
					.format("Remove verification failed. Data is not null. Try #%s. location key = '%s' content key = '%s' versionKey = '%s'",
							removeTries, locationKey, contentKey, versionKey));
			if (versionKey == null) {
				dataManager.remove(locationKey, contentKey).addListener(this);
			} else {
				dataManager.removeVersion(locationKey, contentKey, versionKey).addListener(this);
			}
		} else {
			logger.error(String
					.format("Remove verification failed. Data is not null after %s tries. location key = '%s' content key = '%s'",
							removeTries - 1, locationKey, contentKey, versionKey));
			if (listener != null)
				listener.onRemoveFailure();
		}
	}
}
