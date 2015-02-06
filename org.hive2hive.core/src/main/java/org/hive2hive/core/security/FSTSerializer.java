package org.hive2hive.core.security;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
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
	 * Crate a default serializer with the default security provider (BC). The provider needs to be installed
	 * separately.
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

		// add custom serializer for all public / private keys for full compatibility
		FSTKeyPairSerializer keySerializer = new FSTKeyPairSerializer();
		fst.registerSerializer(KeyPair.class, keySerializer, true);
		fst.registerSerializer(PublicKey.class, keySerializer, true);
		fst.registerSerializer(PrivateKey.class, keySerializer, true);
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private class FSTKeyPairSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTFieldInfo referencedBy,
				int streamPosition) throws IOException {
			if (toWrite instanceof KeyPair) {
				KeyPair keyPair = (KeyPair) toWrite;
				encodeKey(keyPair.getPrivate(), out);
				encodeKey(keyPair.getPublic(), out);
			} else if (toWrite instanceof Key) {
				encodeKey((Key) toWrite, out);
			} else {
				logger.warn("Object to encode is not key or keypair. It is of class {}", toWrite.getClass().getName());
			}
		}

		private void encodeKey(Key key, FSTObjectOutput out) throws IOException {
			out.writeInt(key.getEncoded().length);
			out.write(key.getEncoded());
		}

		@Override
		public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
				FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException,
				InstantiationException, IllegalAccessException {
			if (objectClass.isAssignableFrom(KeyPair.class)) {
				try {
					PrivateKey privateKey = decodePrivateKey(in);
					PublicKey publicKey = decodePublicKey(in);
					KeyPair keyPair = new KeyPair(publicKey, privateKey);
					in.registerObject(keyPair, streamPosition, serializationInfo, referencee);
					return keyPair;
				} catch (GeneralSecurityException e) {
					logger.error("Failed to decode a key pair using a custom FST serializer", e);
				}
			} else if (objectClass.isAssignableFrom(PublicKey.class)) {
				try {
					PublicKey publicKey = decodePublicKey(in);
					in.registerObject(publicKey, streamPosition, serializationInfo, referencee);
					return publicKey;
				} catch (GeneralSecurityException e) {
					logger.error("Failed to decode a public key using a custom FST serializer", e);
				}
			} else if (objectClass.isAssignableFrom(PrivateKey.class)) {
				try {
					PrivateKey privateKey = decodePrivateKey(in);
					in.registerObject(privateKey, streamPosition, serializationInfo, referencee);
					return privateKey;
				} catch (GeneralSecurityException e) {
					logger.error("Failed to decode a private key using a custom FST serializer", e);
				}
			} else {
				logger.warn("Failed to identify class {} as a keypair, a public or a private key");
			}

			return super.instantiate(objectClass, in, serializationInfo, referencee, streamPosition);
		}

		private PublicKey decodePublicKey(FSTObjectInput in) throws IOException, GeneralSecurityException {
			byte[] buffer = new byte[in.readInt()];
			in.read(buffer);
			KeyFactory gen = KeyFactory.getInstance("RSA", securityProvider);
			return gen.generatePublic(new X509EncodedKeySpec(buffer));
		}

		private PrivateKey decodePrivateKey(FSTObjectInput in) throws IOException, GeneralSecurityException {
			byte[] buffer = new byte[in.readInt()];
			in.read(buffer);
			KeyFactory gen = KeyFactory.getInstance("RSA", securityProvider);
			return gen.generatePrivate(new PKCS8EncodedKeySpec(buffer));
		}
	}
}
