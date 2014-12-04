package org.hive2hive.client.util.buffer;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the buffer
 * 
 * @author Nico
 * 
 */
public class BaseFileBufferTest {

	@Test
	public void testBufferFinishesSomewhen() throws IOException, InterruptedException {
		final AtomicInteger counter = new AtomicInteger(0);
		BaseFileBuffer buffer = new BaseFileBuffer(null) {
			@Override
			protected void processBuffer(IFileBufferHolder buffer) {
				counter.set(buffer.getFileBuffer().size());
			}
		};

		File directory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
		buffer.addFileToBuffer(directory);
		buffer.addFileToBuffer(createFileRandomContent(directory));

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

		File directory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
		buffer.addFileToBuffer(directory);
		buffer.addFileToBuffer(createFileRandomContent(directory));

		// sleep for some time
		Thread.sleep((long) (IFileBuffer.BUFFER_WAIT_TIME_MS * 1.4));

		// add another file
		buffer.addFileToBuffer(createFileRandomContent(directory));

		// although it's same buffer, should still be 2
		Assert.assertEquals(2, counter.get());

		// reset to 0
		counter.set(0);

		// wait for the next batch
		Thread.sleep((long) (IFileBuffer.BUFFER_WAIT_TIME_MS * 1.4));

		Assert.assertEquals(1, counter.get());
	}

	public static File createFileRandomContent(File parent) throws IOException {
		// create file of size of multiple numbers of chunks
		File file = new File(parent, UUID.randomUUID().toString());
		FileUtils.write(file, UUID.randomUUID().toString(), true);
		return file;
	}
}
