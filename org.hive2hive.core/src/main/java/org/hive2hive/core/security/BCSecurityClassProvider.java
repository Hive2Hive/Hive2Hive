package org.hive2hive.core.security;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hive2hive.core.serializer.ISecurityClassProvider;

public class BCSecurityClassProvider implements ISecurityClassProvider {

	@Override
	public String getSecurityProvider() {
		return BouncyCastleProvider.PROVIDER_NAME;
	}

	@Override
	public Class<? extends RSAPublicKey> getRSAPublicKeyClass() {
		return BCRSAPublicKey.class;
	}

	@Override
	public Class<? extends RSAPrivateKey> getRSAPrivateKeyClass() {
		return BCRSAPrivateKey.class;
	}

	@Override
	public Class<? extends RSAPrivateCrtKey> getRSAPrivateCrtKeyClass() {
		return BCRSAPrivateCrtKey.class;
	}
}
