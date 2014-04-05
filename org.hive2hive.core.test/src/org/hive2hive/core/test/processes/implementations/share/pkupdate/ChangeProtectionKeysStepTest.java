package org.hive2hive.core.test.processes.implementations.share.pkupdate;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.implementations.context.BasePKUpdateContext;
import org.hive2hive.core.processes.implementations.share.pkupdate.ChangeProtectionKeysStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.H2HSignatureFactory;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.processes.util.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the step that changes the content protection key in the DHT.
 * 
 * @author Seppi
 */
public class ChangeProtectionKeysStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 2;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ChangeProtectionKeysStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testStepSuccessWithChunk() throws InterruptedException, NoPeerConnectionException,
			DataLengthException, InvalidKeyException, IllegalStateException, InvalidCipherTextException,
			IllegalBlockSizeException, BadPaddingException, IOException, SignatureException {
		// where the process runs
		NetworkManager getter = network.get(0);
		// where the data gets stored
		NetworkManager proxy = network.get(1);

		// generate necessary keys
		KeyPair encryptionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_CHUNK);
		KeyPair protectionKeysOld = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKeysNew = EncryptionUtil.generateRSAKeyPair();

		// generate a fake chunk
		Chunk chunk = new Chunk(proxy.getNodeId(), NetworkTestUtil.randomString().getBytes(), 0, 1);
		// encrypt the chunk
		HybridEncryptedContent encryptedChunk = H2HEncryptionUtil.encryptHybrid(chunk,
				encryptionKeys.getPublic());

		// initialize put
		Parameters parameters = new Parameters().setLocationKey(chunk.getId())
				.setContentKey(H2HConstants.FILE_CHUNK).setProtectionKeys(protectionKeysOld)
				.setData(encryptedChunk);
		// indicate to generate hash
		parameters.setHashFlag(true);
		// put encrypted chunk into network
		getter.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		// verify put
		Assert.assertNotNull(getter.getDataManager().getUnblocked(parameters).awaitUninterruptibly()
				.getData());

		// initialize a fake process context
		BasePKUpdateContext context = new TestChunkPKUpdateContext(protectionKeysOld, protectionKeysNew,
				chunk, parameters.getHash());
		// create a change protection keys process step
		ChangeProtectionKeysStep step = new ChangeProtectionKeysStep(context, getter.getDataManager());
		// run process, should not fail
		UseCaseTestUtil.executeProcess(step);

		// verify if signature has changed
		Assert.assertTrue(getter.getDataManager().getUnblocked(parameters).awaitUninterruptibly().getData()
				.verify(protectionKeysNew.getPublic(), new H2HSignatureFactory()));

	}

	@Test
	public void testStepSuccessWithMetaFile() throws InterruptedException, NoPeerConnectionException,
			DataLengthException, InvalidKeyException, IllegalStateException, InvalidCipherTextException,
			IllegalBlockSizeException, BadPaddingException, IOException, SignatureException {
		// where the process runs
		NetworkManager getter = network.get(0);

		// generate necessary keys
		KeyPair chunkEncryptionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_CHUNK);
		KeyPair metaFileEncryptionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		KeyPair protectionKeysOld = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKeysNew = EncryptionUtil.generateRSAKeyPair();

		// generate a fake meta file
		List<MetaChunk> metaChunks1 = new ArrayList<MetaChunk>();
		metaChunks1.add(new MetaChunk(NetworkTestUtil.randomString(), NetworkTestUtil.randomString()
				.getBytes()));
		metaChunks1.add(new MetaChunk(NetworkTestUtil.randomString(), NetworkTestUtil.randomString()
				.getBytes()));
		List<MetaChunk> metaChunks2 = new ArrayList<MetaChunk>();
		metaChunks2.add(new MetaChunk(NetworkTestUtil.randomString(), NetworkTestUtil.randomString()
				.getBytes()));
		List<FileVersion> fileVersions = new ArrayList<FileVersion>();
		fileVersions.add(new FileVersion(0, 123, System.currentTimeMillis(), metaChunks1));
		fileVersions.add(new FileVersion(1, 123, System.currentTimeMillis(), metaChunks2));
		MetaFile metaFile = new MetaFile(metaFileEncryptionKeys.getPublic(), fileVersions,
				chunkEncryptionKeys);
		// encrypt the meta file
		HybridEncryptedContent encryptedMetaFile = H2HEncryptionUtil.encryptHybrid(metaFile,
				metaFileEncryptionKeys.getPublic());
		encryptedMetaFile.generateVersionKey();

		// initialize put
		Parameters parameters = new Parameters()
				.setLocationKey(H2HEncryptionUtil.key2String(metaFile.getId()))
				.setContentKey(H2HConstants.META_FILE).setVersionKey(encryptedMetaFile.getVersionKey())
				.setProtectionKeys(protectionKeysOld).setData(encryptedMetaFile);
		// indicate to generate hash
		parameters.setHashFlag(true);
		// put encrypted meta file into network
		getter.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		// verify put
		Assert.assertNotNull(getter.getDataManager().getUnblocked(parameters).awaitUninterruptibly()
				.getData());

		// initialize a fake process context
		BasePKUpdateContext context = new TestMetaFilePKUpdateContext(protectionKeysOld, protectionKeysNew,
				metaFile, parameters.getHash());
		// create a change protection keys process step
		ChangeProtectionKeysStep step = new ChangeProtectionKeysStep(context, getter.getDataManager());
		// run process, should not fail
		UseCaseTestUtil.executeProcess(step);

		// verify if signature has changed
		Assert.assertTrue(getter.getDataManager().getUnblocked(parameters).awaitUninterruptibly().getData()
				.verify(protectionKeysNew.getPublic(), new H2HSignatureFactory()));

	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private class TestChunkPKUpdateContext extends BasePKUpdateContext {

		private final Chunk chunk;
		private final byte[] hash;

		public TestChunkPKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys, Chunk chunk,
				byte[] hash) {
			super(oldProtectionKeys, newProtectionKeys);
			this.chunk = chunk;
			this.hash = hash;
		}

		@Override
		public String getLocationKey() {
			return chunk.getId();
		}

		@Override
		public String getContentKey() {
			return H2HConstants.FILE_CHUNK;
		}

		@Override
		public int getTTL() {
			return chunk.getTimeToLive();
		}

		@Override
		public byte[] getHash() {
			return hash;
		}

	}

	private class TestMetaFilePKUpdateContext extends BasePKUpdateContext {

		private final MetaFile metaFile;
		private final byte[] hash;

		public TestMetaFilePKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys,
				MetaFile metaFile, byte[] hash) {
			super(oldProtectionKeys, newProtectionKeys);
			this.metaFile = metaFile;
			this.hash = hash;
		}

		@Override
		public String getLocationKey() {
			return H2HEncryptionUtil.key2String(metaFile.getId());
		}

		@Override
		public String getContentKey() {
			return H2HConstants.META_FILE;
		}

		@Override
		public int getTTL() {
			return metaFile.getTimeToLive();
		}

		@Override
		public byte[] getHash() {
			return hash;
		}

	}
}
