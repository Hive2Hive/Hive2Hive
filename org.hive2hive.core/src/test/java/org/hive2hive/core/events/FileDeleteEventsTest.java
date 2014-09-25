package org.hive2hive.core.events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.processes.util.UseCaseTestUtil;
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
		assertEqualsRelativePaths(file.toPath(), events.get(0).getPath());
		assertFalse(Files.exists(events.get(0).getPath()));
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
		
		// check event types
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDeleteEvent.class);
		
		// verify paths
		assertTrue(events.size() == 1);
		assertEqualsRelativePaths(folder.toPath(), events.get(0).getPath());
		assertFalse(Files.exists(events.get(0).getPath()));
	}
	
	@Test 
	public void testFolderWithFilesDeleteEvent() throws NoPeerConnectionException, IOException, NoSessionException { 
		List<File> files = createAndAddFolderWithFiles(rootA, clientA);
		// wait for events on other side (clientB)
		waitForNumberOfEvents(files.size());
		listener.getEvents().clear();
				
		// now delete all files and the folder (reverse order)
		Collections.reverse(files);
		ListIterator<File> it = files.listIterator();
		while(it.hasNext()) {
			UseCaseTestUtil.deleteFile(clientA, it.next());
		}
		
		// wait for delete events on other side (clientB)
		waitForNumberOfEvents(files.size());
				
		// check number of received events and their type
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileDeleteEvent.class);
		assertTrue(events.size() == files.size());
		
		// match file paths of events with uploaded files
		for(int i = 0; i < files.size(); ++i) {
			IFileEvent e = events.get(i);
			File f = files.get(i);
			assertEqualsRelativePaths(f.toPath(), e.getPath());
			assertFalse(Files.exists(e.getPath()));
		}
	}
}
