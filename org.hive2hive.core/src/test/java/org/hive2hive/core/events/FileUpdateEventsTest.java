package org.hive2hive.core.events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.Test;

public class FileUpdateEventsTest extends FileEventsTest {

	static {
		testClass = FileUpdateEventsTest.class;
	}

	@Test
	public void testFileUpdateEvent() throws NoPeerConnectionException, IOException, NoSessionException {
		// upload a file from machine A
		File file = createAndAddFile(rootA, clientA);
		// clear past events of upload
		waitForNumberOfEvents(1);
		listener.getEvents().clear();

		// update the file
		FileUtils.write(file, randomString());
		UseCaseTestUtil.uploadNewVersion(clientA, file);
		// wait for event
		waitForNumberOfEvents(1);

		// check event type
		List<IFileEvent> events = listener.getEvents();
		assertEventType(events, IFileUpdateEvent.class);

		// check paths
		assertTrue(events.size() == 1);
		IFileEvent ev = events.get(0);

		assertTrue(ev.isFile());
		assertFalse(ev.isFolder());
		assertEqualsRelativePaths(file, ev.getFile());
	}

}
