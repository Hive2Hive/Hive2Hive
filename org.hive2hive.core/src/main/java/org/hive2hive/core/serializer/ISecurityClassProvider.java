package org.hive2hive.core.serializer;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Provides classes for the respective security provider to serialize / deserialize using
 * {@link FSTSerializer}.
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
