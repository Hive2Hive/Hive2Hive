package org.hive2hive.core.security;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.UserPublicKey;
import org.hive2hive.core.model.versioned.Locations;
import org.hive2hive.core.model.versioned.MetaFileLarge;
import org.hive2hive.core.model.versioned.MetaFileSmall;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.network.messages.direct.ContactPeerMessage;
import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzInfo.FSTFieldInfo;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
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
	private final String securityProvider;

	/**
	 * Crate a default serializer with the default security provider (BC)
	 */
	public FSTSerializer() {
		this(true, "BC");
	}

	/**
	 * Create a FST serializer (faster and more efficient than Java default serialization).
	 * 
	 * @param useUnsafe <code>true</code> to use <code>sun.misc.Unsafe</code> class, otherwise, a fallback is
	 *            used.
	 * @param securityProvider the security provider, needed to decode key pairs correctly
	 */
	public FSTSerializer(boolean useUnsafe, String securityProvider) {
		this.securityProvider = securityProvider;

		if (!useUnsafe) {
			// don't use sun.misc.Unsafe class
			FSTUtil.unFlaggedUnsafe = null;
			logger.debug("Disabled the use of 'sun.misc.Unsafe' for the serialization");
		}

		fst = FSTConfiguration.createDefaultConfiguration();

		// register all often serialized classes for speedup. Note that every peer should have the same
		// configuration, which also depends on the order!
		fst.registerClass(UserProfile.class, Locations.class, UserPublicKey.class, MetaFileSmall.class, MetaFileLarge.class,
				Chunk.class, ContactPeerMessage.class, ResponseMessage.class);

		// add custom serializer for all keypairs for full compatibility
		fst.registerSerializer(KeyPair.class, new FSTKeyPairSerializer(), true);
	}

	@Override
	public byte[] serialize(Serializable object) throws IOException {
		try {
			return fst.asByteArray(object);
		} catch (Throwable e) {
			logger.error("Exception while serializing object:", e);
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
		} catch (Throwable e) {
			logger.error("Exception while deserializing object.");
			throw e;
		}
	}

	/**
	 * Custom serializer for key pairs because there are problems encoding and decoding bouncy castle keys.<br>
	 * Issue: https://github.com/RuedigerMoeller/fast-serialization/issues/53
	 * 
	 * @author Nico
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class FSTKeyPairSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTFieldInfo referencedBy,
				int streamPosition) throws IOException {
			KeyPair keyPair = (KeyPair) toWrite;
			out.writeInt(keyPair.getPrivate().getEncoded().length);
			out.write(keyPair.getPrivate().getEncoded());

			out.writeInt(keyPair.getPublic().getEncoded().length);
			out.write(keyPair.getPublic().getEncoded());
		}

		@Override
		public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
				FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException,
				InstantiationException, IllegalAccessException {
			try {
				byte[] buffer = new byte[in.readInt()];
				in.read(buffer);
				KeyFactory gen = KeyFactory.getInstance("RSA", securityProvider);
				PrivateKey privateKey = gen.generatePrivate(new PKCS8EncodedKeySpec(buffer));

				buffer = new byte[in.readInt()];
				in.read(buffer);
				PublicKey publicKey = gen.generatePublic(new X509EncodedKeySpec(buffer));

				KeyPair result = new KeyPair(publicKey, privateKey);
				in.registerObject(result, streamPosition, serializationInfo, referencee);
				return result;
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
				logger.error("Failed to decode a keypair using a custom FST serializer", e);
				return super.instantiate(objectClass, in, serializationInfo, referencee, streamPosition);
			}
		}
	}
}
