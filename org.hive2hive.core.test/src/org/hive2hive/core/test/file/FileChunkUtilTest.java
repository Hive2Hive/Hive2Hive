package org.hive2hive.core.test.file;

import org.hive2hive.core.test.H2HJUnitTest;
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

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FileChunkUtilTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Test
	public void testGetNumberOfChunksRandom() {
	}
}
