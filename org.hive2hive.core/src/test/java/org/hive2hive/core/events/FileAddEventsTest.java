package org.hive2hive.core.events;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.List;

import org.hive2hive.core.events.framework.interfaces.file.IFileDownloadEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.FileTestUtil;
import org.hive2hive.core.network.NetworkTestUtil;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
import org.junit.Test;

public class FileAddEventsTest extends FileEventsTest {
	
	static {
		testClass = FileAddEventsTest.class;
	}
	
	@Test
	public void testFileAddEvent() throws NoPeerConnectionException, IOException, NoSessionException {
		// upload a file from machine A
		File file = createAndAddFile(rootA, clientA);
		// wait for the event
		waitForNumberOfEvents(1);
				
		// check event type
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDownloadEvent.class);
		
		// check path
		assertTrue(events.size() == 1);
		IFileEvent ev = events.get(0);
		
		assertTrue(ev.isFile());
		assertFalse(ev.isFolder());
		assertEqualsRelativePaths(file.toPath(), ev.getPath());
		assertTrue(Files.exists(ev.getPath()));
	}
	
	@Test
	public void testBigFileAddEvent() throws NoPeerConnectionException, IOException, NoSessionException {
		// upload a big file from machine A
		BigInteger maxFileSize = clientA.getSession().getFileConfiguration().getMaxFileSize();
		int minChunks = (int) maxFileSize.longValue() / CHUNK_SIZE;
		String fileName = NetworkTestUtil.randomString();
		File file = FileTestUtil.createFileRandomContent(fileName, minChunks + 1, rootA, CHUNK_SIZE);
		UseCaseTestUtil.uploadNewFile(clientA, file);
		
		// wait for the event
		waitForNumberOfEvents(1);
		
		// get event and check type
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDownloadEvent.class);
		
		// check path
		assertTrue(events.size() == 1);
		IFileEvent ev = events.get(0);
		
		assertTrue(ev.isFile());
		assertFalse(ev.isFolder());
		assertEqualsRelativePaths(file.toPath(), ev.getPath());
		assertTrue(Files.exists(ev.getPath()));
	}
	
	@Test
	public void testEmptyFolderAddEvent() throws NoPeerConnectionException, IOException, NoSessionException {
		// create and upload a folder from machine A
		File folder = createAndAddFolder(rootA, clientA);
		// wait for event on B
		waitForNumberOfEvents(1);
		
		// check type of event
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDownloadEvent.class);
		
		// check paths 
		assertTrue(events.size() == 1);
		IFileEvent ev = events.get(0);
		
		assertFalse(ev.isFile());
		assertTrue(ev.isFolder());
		assertEqualsRelativePaths(folder.toPath(), ev.getPath());
		assertTrue(Files.exists(ev.getPath()));
	}
	
	@Test
	public void testFolderWithFilesAddEvent() throws NoPeerConnectionException, IOException, NoSessionException 
	{
		List<File> files = createAndAddFolderWithFiles(rootA, clientA);
		
		// wait for events on other side (clientB)
		waitForNumberOfEvents(files.size());
		
		// check number of received events and their type
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDownloadEvent.class);
		assertTrue(events.size() == files.size());
		
		// match file paths of events with uploaded files
		for(int i = 0; i < events.size(); ++i) {
			IFileEvent ev = events.get(i);
			File f = files.get(i);
			
			assertTrue(f.isFile() == ev.isFile());
			assertTrue(f.isDirectory() == ev.isFolder());
			assertEqualsRelativePaths(f.toPath(), ev.getPath());
			assertTrue(Files.exists(ev.getPath()));
		}
	}
}
