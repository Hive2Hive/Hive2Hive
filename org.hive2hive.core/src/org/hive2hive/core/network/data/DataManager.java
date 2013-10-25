package org.hive2hive.core.network.data;

import java.io.IOException;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.builder.DHTBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;

/**
 * This class offers an interface for storing into and loading from the network.
 * Data can be stored or loaded globally or locally.
 * 
 * @author Seppi
 */
public class DataManager {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(DataManager.class);

	private final NetworkManager networkManager;

	public DataManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	/**
	 * Stores the content into the DHT at the location under the given content
	 * key
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @param wrapper
	 *            the wrapper containing the content to be stored
	 * @return the future
	 */
	public FutureDHT putGlobal(String locationKey, String contentKey, NetworkData wrapper) {
		logger.debug(String.format("global put key = '%s' content key = '%s'", locationKey, contentKey));
		try {
			Data data = new Data(wrapper);
			data.setTTLSeconds(wrapper.getTimeToLive());
			return networkManager.getConnection().getPeer().put(Number160.createHash(locationKey))
					.setData(Number160.createHash(contentKey), data).start();
		} catch (IOException e) {
			logger.error(String
					.format("Global put failed. content = '%s' in the location = '%s' under the contentKey = '%s' exception = '%s'",
							wrapper.toString(), locationKey, contentKey, e.getMessage()));
			return null;
		}
	}

	/**
	 * Loads the content with the given location and content keys from the
	 * DHT.</br> <b>Important:</b>
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @return the future
	 */
	public FutureDHT getGlobal(String locationKey, String contentKey) {
		logger.debug(String.format("global get key = '%s' content key = '%s'", locationKey, contentKey));
		return networkManager.getConnection().getPeer().get(Number160.createHash(locationKey))
				.setContentKey(Number160.createHash(contentKey)).start();
	}

	/**
	 * Stores the given content with the key in the storage of the peer.</br>
	 * The content key allows to store several objects for the same key.
	 * <b>Important:</b> This method blocks till the storage succeeded.
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @param wrapper
	 *            the wrapper containing the content to be stored
	 */
	public void putLocal(String locationKey, String contentKey, NetworkData wrapper) {
		logger.debug(String.format("local put key = '%s' content key = '%s'", locationKey, contentKey));
		try {
			Data data = new Data(wrapper);
			data.setTTLSeconds(wrapper.getTimeToLive());
			networkManager
					.getConnection()
					.getPeer()
					.getPeerBean()
					.getStorage()
					.put(Number160.createHash(locationKey), DHTBuilder.DEFAULT_DOMAIN,
							Number160.createHash(contentKey), data);
		} catch (IOException e) {
			logger.error(String
					.format("Local put failed. content = '%s' in the location = '%s' under the contentKey = '%s' exception = '%s'",
							wrapper.toString(), locationKey, contentKey, e.getMessage()));
		}
	}

	/**
	 * Loads the content with the key directly from the storage of the peer
	 * 
	 * @param locationKey
	 *            the unique id of the content
	 * @param contentKey
	 *            the content key - please choose one from {@link H2HConstants}
	 * @return the desired content from the wrapper
	 */
	public Object getLocal(String locationKey, String contentKey) {
		logger.debug(String.format("local get key = '%s' content key = '%s'", locationKey, contentKey));
		Data data = networkManager
				.getConnection()
				.getPeer()
				.getPeerBean()
				.getStorage()
				.get(Number160.createHash(locationKey), DHTBuilder.DEFAULT_DOMAIN,
						Number160.createHash(contentKey));
		if (data != null) {
			try {
				return data.getObject();
			} catch (ClassNotFoundException | IOException e) {
				logger.error(String.format("local get failed exception = '%s'", e.getMessage()));
			}
		} else {
			logger.warn("futureDHT.getData() is null");
		}
		return null;
	}

	/**
	 * Removes a content from the DHT
	 * 
	 * @param locationKey the unique id of the content
	 * @param contentKey the content key - please choose one from {@link H2HConstants}
	 * @return the future
	 */
	public FutureDHT remove(String locationKey, String contentKey) {
		logger.debug(String.format("remove key = '%s' content key = '%s'", locationKey, contentKey));
		return networkManager.getConnection().getPeer().remove(Number160.createHash(locationKey))
				.setContentKey(Number160.createHash(contentKey)).start();
	}
}
