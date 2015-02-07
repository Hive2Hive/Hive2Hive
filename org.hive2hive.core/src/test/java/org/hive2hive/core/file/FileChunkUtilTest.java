package org.hive2hive.core.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.TestFileConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the file chunk util used for the H2H node.
 * 
 * @author Nico
 * 
 */
public class FileChunkUtilTest extends H2HJUnitTest {

	private static File parent;

	@BeforeClass
	public static void initTest() throws Exception {
		parent = FileTestUtil.getTempDirectory();
		testClass = FileChunkUtilTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Test
	public void testGetNumberOfChunksRandom() throws IOException {
		Random rnd = new Random();
		for (int i = 0; i < 10; i++) {
			int genNOC = rnd.nextInt(100) + 1; // avoid 0's
			File randomFile = FileTestUtil.createFileRandomContent(genNOC, parent);
			int resNOC = FileChunkUtil.getNumberOfChunks(randomFile, TestFileConfiguration.CHUNK_SIZE);
			assertEquals(genNOC, resNOC);

			randomFile.deleteOnExit(); // cleanup
		}
	}

	@Test
	public void testGetNumberOfChunksEmpty() throws IOException {
		File file = new File(parent, randomString());
		FileUtils.write(file, "");
		assertEquals(1, FileChunkUtil.getNumberOfChunks(file, TestFileConfiguration.CHUNK_SIZE));
		assertEquals(1, FileChunkUtil.getNumberOfChunks(file, TestFileConfiguration.CHUNK_SIZE / 2));

		file.deleteOnExit(); // clenaup
	}

	@Test
	public void testGetNumberOfChunksNotExisting() {
		File file = new File(parent, randomString());
		assertEquals(0, FileChunkUtil.getNumberOfChunks(file, TestFileConfiguration.CHUNK_SIZE));
	}

	@Test
	public void testGetNumberOfChunksNull() {
		assertEquals(0, FileChunkUtil.getNumberOfChunks(null, TestFileConfiguration.CHUNK_SIZE));
	}

	@Test
	public void testGetNumberOfChunksSizeZero() throws IOException {
		File file = new File(parent, randomString());
		FileUtils.write(file, "test");
		assertEquals(0, FileChunkUtil.getNumberOfChunks(file, 0));

		file.deleteOnExit(); // clenaup
	}

	@Test
	public void testGetNumberOfChunksSizeNegative() throws IOException {
		File file = new File(parent, randomString());
		FileUtils.write(file, "test");
		assertEquals(0, FileChunkUtil.getNumberOfChunks(file, -1 * TestFileConfiguration.CHUNK_SIZE));

		file.deleteOnExit(); // clenaup
	}

	@Test
	public void testGetChunkRandom() throws IOException {
		Random rnd = new Random();
		for (int i = 0; i < 10; i++) {
			int genNOC = rnd.nextInt(100) + 2; // avoid 0's
			File randomFile = FileTestUtil.createFileRandomContent(genNOC, parent);

			// get chunk 0 ... n-1
			int chosenChunk = rnd.nextInt(genNOC - 1); // index starts at 0
			Chunk chunk = FileChunkUtil.getChunk(randomFile, TestFileConfiguration.CHUNK_SIZE, chosenChunk, randomString());
			assertEquals(TestFileConfiguration.CHUNK_SIZE, chunk.getSize());
			assertEquals(chosenChunk, chunk.getOrder());

			// get last chunk n
			int lastChunkIndex = genNOC - 1; // index starts at 0
			chunk = FileChunkUtil.getChunk(randomFile, TestFileConfiguration.CHUNK_SIZE, lastChunkIndex, randomString());
			assertTrue(TestFileConfiguration.CHUNK_SIZE > chunk.getSize());
			assertEquals(lastChunkIndex, chunk.getOrder());

			randomFile.deleteOnExit(); // cleanup
		}
	}

	@Test
	public void testGetChunkEmpty() throws IOException {
		File file = new File(parent, randomString());
		FileUtils.write(file, "");
		Chunk chunk = FileChunkUtil.getChunk(file, TestFileConfiguration.CHUNK_SIZE, 0, randomString());
		assertEquals(0, chunk.getSize());
		assertEquals(0, chunk.getOrder());

		file.deleteOnExit(); // clenaup
	}

	@Test(expected = IOException.class)
	public void testGetChunkNotExisting() throws IOException {
		File file = new File(parent, randomString());
		FileChunkUtil.getChunk(file, TestFileConfiguration.CHUNK_SIZE, 0, randomString());
	}

	@Test(expected = IOException.class)
	public void testGetChunkNull() throws IOException {
		FileChunkUtil.getChunk(null, TestFileConfiguration.CHUNK_SIZE, 0, randomString());
	}

	@Test(expected = IOException.class)
	public void testGetChunkNegativeChunkSize() throws IOException {
		File file = new File(parent, randomString());
		FileChunkUtil.getChunk(file, -1 * TestFileConfiguration.CHUNK_SIZE, 0, randomString());
	}

	@Test(expected = IOException.class)
	public void testGetChunkNegativeOrderNumber() throws IOException {
		File file = new File(parent, randomString());
		FileChunkUtil.getChunk(file, TestFileConfiguration.CHUNK_SIZE, -10, randomString());
	}

	@Test
	public void testGetChunkTooHighIndex() throws IOException {
		File file = new File(parent, randomString());
		FileUtils.write(file, "test");
		Chunk chunk = FileChunkUtil.getChunk(file, TestFileConfiguration.CHUNK_SIZE, 100, randomString());
		assertNull(chunk);
	}
}
