package org.hive2hive.core.network.data;

import java.security.KeyPair;

import net.tomp2p.peers.Number160;

import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.parameters.IParameters;
import org.hive2hive.core.network.userprofiletask.UserProfileTask;
import org.hive2hive.core.security.IH2HEncryption;

/**
 * This class offers an interface for putting, getting and removing data from the network. All calls are
 * blocking until the put, get or removals are done. Make sure that the calls are not in the same thread as
 * TomP2P is running because this can lead to ugly behavior (messages that don't arrive, puts that are not
 * possible to this peer, ...).
 * 
 * @author Seppi, Nico
 */
public interface IDataManager {

	IH2HEncryption getEncryption();

	/**
	 * Put some content to the DHT
	 * 
	 * @param parameters containing data and keys for routing and signing
	 * @return the success of the put
	 */
	boolean put(IParameters parameters);

	/**
	 * Put some content to the DHT and change its protection key
	 * 
	 * @param parameters containing all necessary keys
	 * @return the success of the put
	 */
	boolean changeProtectionKey(IParameters parameters);

	/**
	 * This is a special put because a {@link UserProfileTask} needs to be put to a certain place in order
	 * that another user finds it.
	 * 
	 * @param userId the user for which the task is intended
	 * @param contentKey the proxy peer of the user (ususally a hash of the user id of the receiver)
	 * @param content the (encrypted) user profile task
	 * @param protectionKeys heavily recommended thus no other user can delete or overwrite the task
	 * @return the success of the put
	 */
	boolean putUserProfileTask(String userId, Number160 contentKey, NetworkContent content, KeyPair protectionKeys);

	/**
	 * Gets some content from the DHT, which is the newest version.
	 * 
	 * @param parameters containing the keys for routing
	 * @return an encrypted or unencrypted content or null if no content was found
	 */
	NetworkContent get(IParameters parameters);

	/**
	 * Gets a specific version of some content from the DHT.
	 * 
	 * @param parameters containing the keys for routing
	 * @return an encrypted or unencrypted content or null if no content was found
	 */
	NetworkContent getVersion(IParameters parameters);

	/**
	 * This is a special get because a {@link UserProfileTask} is stored at a certain pre-defined place.
	 * {@link UserProfileTask}s are ordered as a queue, this call gets the next one in the queue without
	 * removing it.
	 * 
	 * @param userId the user id (mostly the own user id)
	 * @return an encrypted user profile task or null if no task exists
	 */
	NetworkContent getUserProfileTask(String userId);

	/**
	 * Remove some content of the DHT. All versions are removed.
	 * 
	 * @param parameters containing the keys for routing and signing
	 * @return the success of the removal
	 */
	boolean remove(IParameters parameters);

	/**
	 * Removes a specific version of a content in the DHT, not touching other versions. This is mostly
	 * required for roll-backs or to resolve concurrency problems.
	 * 
	 * @param parameters containing the keys for routing and signing
	 * @return the success of the removal
	 */
	boolean removeVersion(IParameters parameters);

	/**
	 * This is a special removal because a {@link UserProfileTask} is stored at a certain pre-defined place.
	 * This call removes the head of the queue and must only be called when the task has been processed.
	 * 
	 * @param userId the user id (mostly the own user id)
	 * @param contentKey the content key (which type of document)
	 * @param protectionKey if the content is protected, the very same protection keys must be handed as well
	 *            to be able to delete the content
	 * @return the success of the removal
	 */
	boolean removeUserProfileTask(String userId, Number160 contentKey, KeyPair protectionKey);
}
