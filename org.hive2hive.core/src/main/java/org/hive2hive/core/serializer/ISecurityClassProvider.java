package org.hive2hive.core.serializer;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Provides classes for the respective security provider to serialize / deserialize using
 * {@link FSTSerializer}. The problem is that different security providers use different key implementations.
 * BouncyCastle uses for example "org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey" whereas
 * SpongyCastle uses "org.spongycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey" and default Android
 * security providers uses "com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey". These
 * class names are by default serialized and sent over the network. Recipients with other security providers
 * cannot find the class by their names during decoding. Therefore, just indexes are sent over the network and
 * mapped back to the key implementation stated through this implementation.
 * 
 * @author Nico
 *
 */
public interface ISecurityClassProvider {

	String getSecurityProvider();

	Class<? extends RSAPublicKey> getRSAPublicKeyClass();

	Class<? extends RSAPrivateKey> getRSAPrivateKeyClass();

	Class<? extends RSAPrivateCrtKey> getRSAPrivateCrtKeyClass();
}
