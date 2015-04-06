package org.hive2hive.core.integration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.H2HNode;
import org.hive2hive.core.api.configs.FileConfiguration;
import org.hive2hive.core.api.interfaces.IFileConfiguration;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.NetworkTestUtil;
import org.hive2hive.core.utils.TestExecutionUtil;
import org.hive2hive.core.utils.helper.TestFileAgent;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration tests for the H2HNode.
 * 
 * @author Nico
 * 
 */
public class H2HNodeTest extends H2HJUnitTest {

	private static List<IH2HNode> network;

	private static IH2HNode loggedInNode;
	private static UserCredentials credentials;
	private static TestFileAgent fileAgent;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = H2HNodeTest.class;
		beforeClass();
		network = NetworkTestUtil.createH2HNetwork(5);

		credentials = generateRandomCredentials();

		IH2HNode registerNode = network.get(0);
		IProcessComponent<?> registerProcess = registerNode.getUserManager().createRegisterProcess(credentials);
		TestExecutionUtil.executeProcessTillSucceded(registerProcess);

		fileAgent = new TestFileAgent();
		loggedInNode = network.get(1);
		IProcessComponent<Void> loginProcess = loggedInNode.getUserManager().createLoginProcess(credentials, fileAgent);
		TestExecutionUtil.executeProcessTillSucceded(loginProcess);
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownH2HNetwork(network);
		afterClass();
	}

	@Test
	public void testAddDeleteFile() throws IOException, NoSessionException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException, NoPeerConnectionException {
		File testFile = new File(fileAgent.getRoot(), "test-file1");
		FileUtils.write(testFile, "Hello World 1");

		IProcessComponent<Void> process = loggedInNode.getFileManager().createAddProcess(testFile);
		TestExecutionUtil.executeProcessTillSucceded(process);

		// is now added; delete it
		process = loggedInNode.getFileManager().createDeleteProcess(testFile);
		TestExecutionUtil.executeProcessTillSucceded(process);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddFileWrongDir() throws IOException, NoPeerConnectionException, NoSessionException,
			IllegalArgumentException {
		File testFile = new File(FileTestUtil.getTempDirectory(), "test-file2");
		FileUtils.write(testFile, "Hello World 2");

		loggedInNode.getFileManager().createAddProcess(testFile);
	}

	@Test
	public void getPeer() {
		// a unconnected node does not provide a peer
		IFileConfiguration fileConfig = FileConfiguration.createDefault();
		IH2HNode node = H2HNode.createNode(fileConfig);
		Assert.assertNull(node.getPeer());

		// connected nodes return a peer
		for (IH2HNode connectedNode : network) {
			Assert.assertNotNull(connectedNode.getPeer());
		}
	}
}
