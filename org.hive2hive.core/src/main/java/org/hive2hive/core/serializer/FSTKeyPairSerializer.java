package org.hive2hive.core.serializer;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzInfo.FSTFieldInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom serializer for key pairs because there are problems encoding and decoding bouncy castle keys.<br>
 * Issue: https://github.com/RuedigerMoeller/fast-serialization/issues/53
 * 
 * @author Nico
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FSTKeyPairSerializer extends FSTBasicObjectSerializer {

	private static final Logger logger = LoggerFactory.getLogger(FSTKeyPairSerializer.class);
	private final String securityProvider;

	public FSTKeyPairSerializer(String securityProvider) {
		this.securityProvider = securityProvider;
	}

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
	public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTFieldInfo referencee,
			int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
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
