package org.hive2hive.core.network.data;

import java.security.KeyPair;

import net.tomp2p.p2p.builder.DigestBuilder;
import net.tomp2p.peers.Number160;

/**
 * 
 * @author Nico, Seppi
 * 
 */
// TODO documentation
public interface IDataManager {

	boolean put(String locationKey, String contentKey, NetworkContent content, KeyPair protectionKey);

	boolean putUserProfileTask(String userId, Number160 contentKey, NetworkContent content,
			KeyPair protectionKey);

	NetworkContent get(String locationKey, String contentKey);

	NetworkContent getUserProfileTask(String userId);

	boolean remove(String locationKey, String contentKey, KeyPair protectionKey);

	boolean remove(String locationKey, String contentKey, Number160 versionKey, KeyPair protectionKey);

	boolean removeUserProfileTask(String userId, Number160 contentKey, KeyPair protectionKey);

	DigestBuilder getDigest(Number160 locationKey);

}
