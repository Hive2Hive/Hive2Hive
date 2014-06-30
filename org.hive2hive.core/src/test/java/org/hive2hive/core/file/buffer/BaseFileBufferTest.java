package org.hive2hive.core.file.buffer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.file.FileTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the file chunk util used for the H2H node.
 * 
 * @author Nico
 * 
 */
public class BaseFileBufferTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = BaseFileBufferTest.class;
		beforeClass();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	@Test
	public void testBufferFinishesSomewhen() throws IOException, InterruptedException {
		final AtomicInteger counter = new AtomicInteger(0);
		BaseFileBuffer buffer = new BaseFileBuffer(null) {
			@Override
			protected void processBuffer(IFileBufferHolder buffer) {
				counter.set(buffer.getFileBuffer().size());
			}
		};

		File directory = FileTestUtil.getTempDirectory();
		buffer.addFileToBuffer(directory);
		buffer.addFileToBuffer(FileTestUtil.createFileRandomContent(1, directory, 64));

		// sleep for some time
		Thread.sleep((long) (IFileBuffer.BUFFER_WAIT_TIME_MS * 1.5));

		// added two files, buffer should contain two files too
		Assert.assertEquals(2, counter.get());
	}

	@Test
	public void testBufferRunsInBatches() throws IOException, InterruptedException {
		final AtomicInteger counter = new AtomicInteger(0);
		BaseFileBuffer buffer = new BaseFileBuffer(null) {
			@Override
			protected void processBuffer(IFileBufferHolder buffer) {
				counter.addAndGet(buffer.getFileBuffer().size());
			}
		};

		File directory = FileTestUtil.getTempDirectory();
		buffer.addFileToBuffer(directory);
		buffer.addFileToBuffer(FileTestUtil.createFileRandomContent(1, directory, 64));

		// sleep for some time
		Thread.sleep((long) (IFileBuffer.BUFFER_WAIT_TIME_MS * 1.5));

		// add another file
		buffer.addFileToBuffer(FileTestUtil.createFileRandomContent(1, directory, 64));

		// although it's same buffer, should still be 2
		Assert.assertEquals(2, counter.get());

		// reset to 0
		counter.set(0);

		// wait for the next batch
		Thread.sleep(IFileBuffer.BUFFER_WAIT_TIME_MS);

		Assert.assertEquals(1, counter.get());
	}
}
