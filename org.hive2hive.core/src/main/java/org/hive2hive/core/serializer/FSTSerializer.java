package org.hive2hive.core.serializer;

import java.io.IOException;
import java.io.Serializable;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerSocketAddress;

import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.model.versioned.MetaFileLarge;
import org.hive2hive.core.model.versioned.MetaFileSmall;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.messages.AcceptanceReply;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.security.BCSecurityClassProvider;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.util.FSTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fast serialization using the
 * <a href="https://github.com/RuedigerMoeller/fast-serialization">FST library</a>.
 * 
 * @author Nico, Chris
 * 
 */
public final class FSTSerializer implements IH2HSerialize {

	private static final Logger logger = LoggerFactory.getLogger(FSTSerializer.class);

	// thread safe usage of FST
	private final FSTConfiguration fst;

	/**
	 * Crate a default serializer with the default security provider (BC). The provider needs to be installed
	 * separately.
	 */
	public FSTSerializer() {
		this(true, new BCSecurityClassProvider());
	}

	/**
	 * Create a FST serializer (faster and more efficient than Java default serialization).
	 * 
	 * @param useUnsafe <code>true</code> to use <code>sun.misc.Unsafe</code> class, otherwise, a fallback is
	 *            used.
	 * @param securityProvider the security provider, needed to decode key pairs correctly
	 */
	public FSTSerializer(boolean useUnsafe, ISecurityClassProvider securityProvider) {
		if (!useUnsafe) {
			// don't use sun.misc.Unsafe class
			FSTUtil.unFlaggedUnsafe = null;
			logger.debug("Disabled the use of 'sun.misc.Unsafe' for the serialization");
		}

		fst = FSTConfiguration.createDefaultConfiguration();

		// register all often serialized classes for speedup. Note that every peer should have the same
		// configuration, which also depends on the order! Changing these registrations might break backward
		// compatibility!
		fst.registerClass(UserProfile.class, FolderIndex.class, FileIndex.class, UserPermission.class, Locations.class,
				UserPublicKey.class, MetaFileSmall.class, MetaFileLarge.class, FileVersion.class, MetaChunk.class,
				Chunk.class, EncryptedNetworkContent.class, ContactPeerMessage.class, ResponseMessage.class);

		// for all public / private keys for full compatibility among multiple security providers
		fst.registerClass(securityProvider.getRSAPublicKeyClass(), securityProvider.getRSAPrivateKeyClass(),
				securityProvider.getRSAPrivateCrtKeyClass());

		// for performance improvements, native TomP2P classes which are serialized quite often
		fst.registerClass(PeerAddress.class, PeerSocketAddress.class, Number160.class);

		// Since PeerAddresses are serialized very often, this is just an efficiency improvement
		fst.registerSerializer(PeerAddress.class, new FSTPeerAddressSerializer(), false);

		// register the acceptance reply enum
		fst.registerClass(AcceptanceReply.class);
	}

	@Override
	public byte[] serialize(Serializable object) throws IOException {
		try {
			return fst.asByteArray(object);
		} catch (Exception e) {
			logger.error("Exception while serializing object {}", object, e);
			throw e;
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null || bytes.length == 0) {
			// nothing to deserialize
			return null;
		}

		try {
			return fst.asObject(bytes);
		} catch (Exception e) {
			logger.error("Exception while deserializing object.");
			throw e;
		}
	}
}
