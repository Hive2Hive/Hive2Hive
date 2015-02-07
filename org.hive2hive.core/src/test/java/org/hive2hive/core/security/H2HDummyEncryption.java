package org.hive2hive.core.security;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hive2hive.core.model.BaseNetworkContent;
import org.hive2hive.core.model.versioned.EncryptedNetworkContent;
import org.hive2hive.core.model.versioned.HybridEncryptedContent;
import org.hive2hive.core.serializer.FSTSerializer;
import org.hive2hive.core.serializer.IH2HSerialize;

/**
 * Dummy encryption class that does not really encrypt but just serializes the object. This class should
 * <strong>not</strong> be used in productive environments because it does not provide any security!
 * 
 * @author Nico
 * 
 */
public class H2HDummyEncryption implements IH2HEncryption {

	private final IH2HSerialize serializer;
	private static String SECURITY_PROVIDER = "BC";

	public H2HDummyEncryption() {
		serializer = new FSTSerializer();

		// install the provider anyway because probably key pairs need to be generated
		if (Security.getProvider(SECURITY_PROVIDER) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	@Override
	public String getSecurityProvider() {
		return SECURITY_PROVIDER;
	}

	@Override
	public EncryptedNetworkContent encryptAES(BaseNetworkContent content, SecretKey aesKey) throws IOException {
		return new EncryptedNetworkContent(serializer.serialize(content), new byte[] {});
	}

	@Override
	public BaseNetworkContent decryptAES(EncryptedNetworkContent content, SecretKey aesKey) throws IOException,
			ClassNotFoundException {
		return (BaseNetworkContent) serializer.deserialize(content.getCipherContent());
	}

	@Override
	public HybridEncryptedContent encryptHybrid(BaseNetworkContent content, PublicKey publicKey) throws IOException {
		return new HybridEncryptedContent(new byte[] {}, serializer.serialize(content));
	}

	@Override
	public HybridEncryptedContent encryptHybrid(byte[] content, PublicKey publicKey) {
		return new HybridEncryptedContent(new byte[] {}, content);
	}

	@Override
	public BaseNetworkContent decryptHybrid(HybridEncryptedContent content, PrivateKey privateKey) throws IOException,
			ClassNotFoundException {
		return (BaseNetworkContent) serializer.deserialize(content.getEncryptedData());
	}

	@Override
	public byte[] decryptHybridRaw(HybridEncryptedContent content, PrivateKey privateKey) throws ClassNotFoundException,
			IOException {
		return content.getEncryptedData();
	}
}
