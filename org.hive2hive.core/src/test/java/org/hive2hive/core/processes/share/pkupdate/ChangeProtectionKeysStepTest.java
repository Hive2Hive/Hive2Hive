package org.hive2hive.core.processes.share.pkupdate;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import net.tomp2p.peers.Number160;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.MetaChunk;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.network.data.parameters.Parameters;
import org.hive2hive.core.processes.context.BasePKUpdateContext;
import org.hive2hive.core.processes.share.pkupdate.ChangeProtectionKeysStep;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HDefaultEncryption;
import org.hive2hive.core.security.H2HDummyEncryption;
import org.hive2hive.core.security.HybridEncryptedContent;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.util.TestExecutionUtil;
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

	private static final int networkSize = 2;
	private static List<NetworkManager> network;
	private static H2HDummyEncryption dummyEncryption;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ChangeProtectionKeysStepTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
		dummyEncryption = new H2HDummyEncryption();
	}

	@Test
	public void testStepSuccessAndRollbackWithChunk() throws InterruptedException, NoPeerConnectionException,
			DataLengthException, InvalidKeyException, IllegalStateException, InvalidCipherTextException,
			IllegalBlockSizeException, BadPaddingException, IOException, SignatureException, InvalidProcessStateException {
		// where the process runs
		NetworkManager getter = network.get(0);
		// where the data gets stored
		NetworkManager proxy = network.get(1);

		// generate necessary keys
		KeyPair encryptionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_CHUNK);
		KeyPair protectionKeysOld = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKeysNew = EncryptionUtil.generateRSAKeyPair();

		// generate a fake chunk
		Chunk chunk = new Chunk(proxy.getNodeId(), NetworkTestUtil.randomString().getBytes(), 0);
		// encrypt the chunk
		HybridEncryptedContent encryptedChunk = dummyEncryption.encryptHybrid(chunk, encryptionKeys.getPublic());

		// initialize put
		Parameters parameters = new Parameters().setLocationKey(chunk.getId()).setContentKey(H2HConstants.FILE_CHUNK)
				.setProtectionKeys(protectionKeysOld).setData(encryptedChunk);
		// indicate to generate hash
		parameters.setHashFlag(true);
		// put encrypted chunk into network
		getter.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		// verify put
		Assert.assertNotNull(getter.getDataManager().getUnblocked(parameters).awaitUninterruptibly().getData());

		// initialize a fake process context
		BasePKUpdateContext context = new TestChunkPKUpdateContext(protectionKeysOld, protectionKeysNew, chunk,
				parameters.getHash());
		// create a change protection keys process step
		ChangeProtectionKeysStep step = new ChangeProtectionKeysStep(context, getter.getDataManager());
		// run process, should not fail
		TestExecutionUtil.executeProcessTillSucceded(step);

		// verify if content protection keys have changed
		Assert.assertEquals(protectionKeysNew.getPublic(), getter.getDataManager().getUnblocked(parameters)
				.awaitUninterruptibly().getData().publicKey());

		// manually trigger roll back
		step.cancel(new RollbackReason("Testing rollback."));

		// verify if content protection keys have changed to old ones
		Assert.assertEquals(protectionKeysOld.getPublic(), getter.getDataManager().getUnblocked(parameters)
				.awaitUninterruptibly().getData().publicKey());
	}

	@Test
	public void testStepSuccessAndRollbackWithMetaFile() throws InterruptedException, NoPeerConnectionException,
			DataLengthException, InvalidKeyException, IllegalStateException, InvalidCipherTextException,
			IllegalBlockSizeException, BadPaddingException, IOException, SignatureException, InvalidProcessStateException {
		// where the process runs
		NetworkManager getter = network.get(0);

		// generate necessary keys
		KeyPair chunkEncryptionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_CHUNK);
		KeyPair metaFileEncryptionKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);
		KeyPair protectionKeysOld = EncryptionUtil.generateRSAKeyPair();
		KeyPair protectionKeysNew = EncryptionUtil.generateRSAKeyPair();

		// generate a fake meta file
		List<MetaChunk> metaChunks1 = new ArrayList<MetaChunk>();
		metaChunks1.add(new MetaChunk(NetworkTestUtil.randomString(), NetworkTestUtil.randomString().getBytes(), 0));
		metaChunks1.add(new MetaChunk(NetworkTestUtil.randomString(), NetworkTestUtil.randomString().getBytes(), 1));
		List<MetaChunk> metaChunks2 = new ArrayList<MetaChunk>();
		metaChunks2.add(new MetaChunk(NetworkTestUtil.randomString(), NetworkTestUtil.randomString().getBytes(), 2));
		List<FileVersion> fileVersions = new ArrayList<FileVersion>();
		fileVersions.add(new FileVersion(0, 123, System.currentTimeMillis(), metaChunks1));
		fileVersions.add(new FileVersion(1, 123, System.currentTimeMillis(), metaChunks2));
		MetaFileSmall metaFileSmall = new MetaFileSmall(metaFileEncryptionKeys.getPublic(), fileVersions,
				chunkEncryptionKeys);
		// encrypt the meta file
		HybridEncryptedContent encryptedMetaFile = dummyEncryption.encryptHybrid(metaFileSmall,
				metaFileEncryptionKeys.getPublic());
		encryptedMetaFile.generateVersionKey();

		// initialize put
		Parameters parameters = new Parameters().setLocationKey(metaFileSmall.getId()).setContentKey(H2HConstants.META_FILE)
				.setVersionKey(encryptedMetaFile.getVersionKey()).setProtectionKeys(protectionKeysOld)
				.setData(encryptedMetaFile);
		// indicate to generate hash
		parameters.setHashFlag(true);
		// put encrypted meta file into network
		getter.getDataManager().putUnblocked(parameters).awaitUninterruptibly();

		// verify put
		Assert.assertNotNull(getter.getDataManager().getUnblocked(parameters).awaitUninterruptibly().getData());

		// initialize a fake process context
		BasePKUpdateContext context = new TestMetaFilePKUpdateContext(protectionKeysOld, protectionKeysNew, metaFileSmall,
				parameters.getHash(), encryptedMetaFile.getVersionKey());
		// create a change protection keys process step
		ChangeProtectionKeysStep step = new ChangeProtectionKeysStep(context, getter.getDataManager());
		// run process, should not fail
		TestExecutionUtil.executeProcessTillSucceded(step);

		// verify if content protection keys have changed
		Assert.assertEquals(protectionKeysNew.getPublic(), getter.getDataManager().getUnblocked(parameters)
				.awaitUninterruptibly().getData().publicKey());

		// manually trigger roll back
		step.cancel(new RollbackReason("Testing rollback."));

		// verify if content protection keys have changed to old ones
		Assert.assertEquals(protectionKeysOld.getPublic(), getter.getDataManager().getUnblocked(parameters)
				.awaitUninterruptibly().getData().publicKey());
	}

	@AfterClass
	public static void endTest() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	private class TestChunkPKUpdateContext extends BasePKUpdateContext {

		private final Chunk chunk;
		private final byte[] hash;

		public TestChunkPKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys, Chunk chunk, byte[] hash) {
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

		@Override
		public Number160 getVersionKey() {
			return H2HConstants.TOMP2P_DEFAULT_KEY;
		}

	}

	private class TestMetaFilePKUpdateContext extends BasePKUpdateContext {

		private final MetaFileSmall metaFileSmall;
		private final byte[] hash;
		private final Number160 versionKey;

		public TestMetaFilePKUpdateContext(KeyPair oldProtectionKeys, KeyPair newProtectionKeys,
				MetaFileSmall metaFileSmall, byte[] hash, Number160 versionKey) {
			super(oldProtectionKeys, newProtectionKeys);
			this.metaFileSmall = metaFileSmall;
			this.hash = hash;
			this.versionKey = versionKey;
		}

		@Override
		public String getLocationKey() {
			return H2HDefaultEncryption.key2String(metaFileSmall.getId());
		}

		@Override
		public String getContentKey() {
			return H2HConstants.META_FILE;
		}

		@Override
		public int getTTL() {
			return metaFileSmall.getTimeToLive();
		}

		@Override
		public byte[] getHash() {
			return hash;
		}

		@Override
		public Number160 getVersionKey() {
			return versionKey;
		}

	}
}
