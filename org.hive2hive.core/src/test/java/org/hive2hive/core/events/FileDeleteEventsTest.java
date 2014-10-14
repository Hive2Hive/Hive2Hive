package org.hive2hive.core.events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.Test;

public class FileDeleteEventsTest extends FileEventsTest {
	
	static {
		testClass = FileDeleteEventsTest.class;
	}
	
	@Test
	public void testFileDeleteEvent() throws NoPeerConnectionException, IOException, NoSessionException {
		// upload a file from machine A
		File file = createAndAddFile(rootA, clientA);
		// clear past events of upload
		waitForNumberOfEvents(1);
		listener.getEvents().clear(); 
		
		// delete the file
		UseCaseTestUtil.deleteFile(clientA, file);
		// wait for event
		waitForNumberOfEvents(1);
		
		// check event type
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDeleteEvent.class);
		
		// check paths
		assertTrue(events.size() == 1);
		IFileEvent ev = events.get(0);
		
		assertTrue(ev.isFile());
		assertFalse(ev.isFolder());
		assertEqualsRelativePaths(file.toPath(), ev.getPath());
		assertFalse(Files.exists(ev.getPath()));
	}
	
	@Test
	public void testEmptyFolderDeleteEvent() throws NoPeerConnectionException, IOException, NoSessionException {
		// create and upload a folder from machine A
		File folder = createAndAddFolder(rootA, clientA);
		// wait and clear past events
		waitForNumberOfEvents(1);
		listener.getEvents().clear();
		
		// delete folder
		UseCaseTestUtil.deleteFile(clientA, folder);		
		waitForNumberOfEvents(1);
		
		// check event types and path
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDeleteEvent.class);
		
		// verify paths
		assertTrue(events.size() == 1);
		IFileEvent ev = events.get(0);
		
		assertFalse(ev.isFile());
		assertTrue(ev.isFolder());
		assertEqualsRelativePaths(folder.toPath(), ev.getPath());
		assertFalse(Files.exists(ev.getPath()));
	}
	
	@Test 
	public void testFolderWithFilesDeleteEvent() throws NoPeerConnectionException, IOException, NoSessionException { 
		List<File> files = createAndAddFolderWithFiles(rootA, clientA);
		List<Boolean> isFile = new ArrayList<Boolean>();
		// wait for events on other side (clientB)
		waitForNumberOfEvents(files.size());
		listener.getEvents().clear();
				
		// now delete all files and the folder (reverse order)
		Collections.reverse(files);
		ListIterator<File> it = files.listIterator();
		while(it.hasNext()) {
			// since the file will be gone, we cannot determine later whether it was a file or folder, hence save it here.
			File f = it.next();
			isFile.add(f.isFile());
			UseCaseTestUtil.deleteFile(clientA, f);
		}
		
		// wait for delete events on other side (clientB)
		waitForNumberOfEvents(files.size());
				
		// check number of received events and their type
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDeleteEvent.class);
		assertTrue(events.size() == files.size());
		
		// match file paths of events with uploaded files
		for(int i = 0; i < files.size(); ++i) {
			IFileEvent ev = events.get(i);
			File f = files.get(i);
			
			assertTrue(isFile.get(i) == ev.isFile());
			assertTrue(!isFile.get(i) == ev.isFolder());
			assertEqualsRelativePaths(f.toPath(), ev.getPath());
			assertFalse(Files.exists(ev.getPath()));
		}
	}
}
