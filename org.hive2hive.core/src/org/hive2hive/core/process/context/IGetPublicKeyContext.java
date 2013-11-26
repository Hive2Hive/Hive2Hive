package org.hive2hive.core.process.context;

import java.security.PublicKey;

public interface IGetPublicKeyContext {

	void setPublicKey(PublicKey publicKey);

	PublicKey getPublicKey();
}
