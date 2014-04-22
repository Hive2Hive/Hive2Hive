package org.hive2hive.core.file;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.api.H2HFileObserver;
import org.hive2hive.core.api.interfaces.IFileObserver;
import org.hive2hive.core.api.interfaces.IFileObserverListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class H2HFileObserverTest extends H2HJUnitTest {

	// TODO the missing event order tests should be implemented

	enum Event {
		FILE_CREATED,
		FILE_DELETED,
		FILE_CHANGED,
		DIRECTORY_CREATED,
		DIRECTORY_DELETED,
		DIRECTORY_CHANGED
	}

	enum Relation {
		PARENT,
		SELF,
		CHILD
	}

	private IFileObserver testObserver;
	private static int WAIT = 2000;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = H2HFileObserverTest.class;
		beforeClass();
	}

	@Before
	public void createTestDirectory() throws Exception {
		if (Files.exists(getTestDirectoryRoot().toPath(), LinkOption.NOFOLLOW_LINKS)) {
			FileUtils.deleteDirectory(getTestDirectoryRoot());
		}
		FileUtils.forceMkdir(getTestDirectoryRoot());

		testObserver = new H2HFileObserver(getTestDirectoryRoot());
	}

	@After
	public void removeTestDirectory() throws Exception {
		try {
			testObserver.stop();
		} catch (Exception e) {
		}
		testObserver = null;

		FileUtils.deleteDirectory(getTestDirectoryRoot());
	}

	@Test
	public void listenerTest() throws Exception {

		final boolean[] notifiedEvent = new boolean[] { false, false, false, false, false, false, false,
				false };

		IFileObserverListener listener = new IFileObserverListener() {

			@Override
			public void onStop(FileAlterationObserver observer) {
				notifiedEvent[0] = true;
			}

			@Override
			public void onStart(FileAlterationObserver observer) {
				notifiedEvent[1] = true;
			}

			@Override
			public void onFileDelete(File file) {
				notifiedEvent[2] = true;
			}

			@Override
			public void onFileCreate(File file) {
				notifiedEvent[3] = true;
			}

			@Override
			public void onFileChange(File file) {
				notifiedEvent[4] = true;
			}

			@Override
			public void onDirectoryDelete(File directory) {
				notifiedEvent[5] = true;
			}

			@Override
			public void onDirectoryCreate(File directory) {
				notifiedEvent[6] = true;
			}

			@Override
			public void onDirectoryChange(File directory) {
				notifiedEvent[7] = true;
			}
		};
		testObserver.addFileObserverListener(listener);
		testObserver.start();

		// trigger all events
		File subDirectory = Paths.get(getTestDirectoryRoot().getAbsolutePath(), "SubFolderTest").toFile();
		FileUtils.forceMkdir(subDirectory);
		File file = new File(getTestDirectoryRoot(), "File.txt");
		file.createNewFile();
		FileUtils.write(file, "write test");
		FileUtils.moveFileToDirectory(file, subDirectory, true);
		FileUtils.deleteQuietly(file);
		FileUtils.deleteQuietly(subDirectory);

		Thread.sleep(WAIT);

		// check whether all events were triggered
		for (int i = 0; i < notifiedEvent.length; i++) {
			// logger.debug(String.format("[%s]: %s", i+1, notifiedEvent[i]));
			assertTrue(notifiedEvent[i]);
		}

		testObserver.removeFileObserverListener(listener);
		testObserver.stop();
	}

	@Test
	public void fileCreatedRootTest() throws Exception {

		// on root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.FILE_CREATED, Relation.SELF));

		File fileToCreate = new File(getTestDirectoryRoot(), "CreatedFile.txt");
		FileEventOrderListener orderListener = new FileEventOrderListener(fileToCreate);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		fileToCreate.createNewFile();

		Thread.sleep(WAIT);

		assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	@Test
	public void fileCreatedTest() throws Exception {

		// below root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.DIRECTORY_CHANGED, Relation.PARENT));
		expectedOrder.add(new EventCheck(Event.FILE_CREATED, Relation.SELF));

		File subDirectory = Paths.get(getTestDirectoryRoot().getAbsolutePath(), "SubFolder").toFile();
		FileUtils.forceMkdir(subDirectory);
		File fileToCreate = new File(subDirectory, "CreatedFile.txt");
		FileEventOrderListener orderListener = new FileEventOrderListener(fileToCreate);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		fileToCreate.createNewFile();

		Thread.sleep(WAIT);

		assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	@Test
	public void fileDeletedRootTest() throws Exception {

		// on root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.FILE_DELETED, Relation.SELF));

		File fileToDelete = new File(getTestDirectoryRoot(), "FileToDelete.txt");
		fileToDelete.createNewFile();
		FileEventOrderListener orderListener = new FileEventOrderListener(fileToDelete);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		FileUtils.deleteQuietly(fileToDelete);

		Thread.sleep(WAIT);

		assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	@Test
	public void fileDeletedTest() throws Exception {

		// below root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.DIRECTORY_CHANGED, Relation.PARENT));
		expectedOrder.add(new EventCheck(Event.FILE_DELETED, Relation.SELF));

		File subDirectory = Paths.get(getTestDirectoryRoot().getAbsolutePath(), "SubFolder").toFile();
		FileUtils.forceMkdir(subDirectory);
		File fileToDelete = new File(subDirectory, "FileToDelete.txt");
		fileToDelete.createNewFile();
		FileEventOrderListener orderListener = new FileEventOrderListener(fileToDelete);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		FileUtils.deleteQuietly(fileToDelete);

		Thread.sleep(WAIT);

		assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	@Test
	public void fileChangedRootTest() throws Exception {

		// on root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.FILE_CHANGED, Relation.SELF));

		File fileToChange = new File(getTestDirectoryRoot(), "FileToChange.txt");
		fileToChange.createNewFile();
		FileEventOrderListener orderListener = new FileEventOrderListener(fileToChange);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		FileUtils.write(fileToChange, "modified");

		Thread.sleep(WAIT);

		// assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));
		assertTrue(containsOnly(orderListener.getRealOrder(), expectedOrder.get(0)));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	@Test
	public void fileChangedTest() throws Exception {

		// below root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.FILE_CHANGED, Relation.SELF));

		File subDirectory = Paths.get(getTestDirectoryRoot().getAbsolutePath(), "SubFolder").toFile();
		FileUtils.forceMkdir(subDirectory);
		File fileToChange = new File(subDirectory, "FileToChange.txt");
		fileToChange.createNewFile();
		FileEventOrderListener orderListener = new FileEventOrderListener(fileToChange);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		FileUtils.write(fileToChange, "modified");

		Thread.sleep(WAIT);

		// assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));
		assertTrue(containsOnly(orderListener.getRealOrder(), expectedOrder.get(0)));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	@Test
	public void directoryCreatedRootTest() throws Exception {

		// on root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.DIRECTORY_CREATED, Relation.SELF));

		File directoryToCreate = new File(getTestDirectoryRoot(), "CreatedDirectory");
		FileEventOrderListener orderListener = new FileEventOrderListener(directoryToCreate);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		FileUtils.forceMkdir(directoryToCreate);

		Thread.sleep(WAIT);

		assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	@Test
	public void directoryCreatedTest() throws Exception {

		// below root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.DIRECTORY_CHANGED, Relation.PARENT));
		expectedOrder.add(new EventCheck(Event.DIRECTORY_CREATED, Relation.SELF));

		File subDirectory = Paths.get(getTestDirectoryRoot().getAbsolutePath(), "SubFolder").toFile();
		FileUtils.forceMkdir(subDirectory);
		File directoryToCreate = new File(subDirectory, "CreatedDirectory");
		FileEventOrderListener orderListener = new FileEventOrderListener(directoryToCreate);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		FileUtils.forceMkdir(directoryToCreate);

		Thread.sleep(WAIT);

		assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	@Ignore
	@Test
	public void directoryDeletedRootTest() throws Exception {

		// on root level
		List<EventCheck> expectedOrder = new ArrayList<EventCheck>();
		expectedOrder.add(new EventCheck(Event.FILE_DELETED, Relation.CHILD));
		expectedOrder.add(new EventCheck(Event.DIRECTORY_DELETED, Relation.CHILD));
		expectedOrder.add(new EventCheck(Event.DIRECTORY_DELETED, Relation.SELF));

		File directoryToDelete = new File(getTestDirectoryRoot(), "DirectoryToDelete");
		FileUtils.forceMkdir(directoryToDelete);
		File childFile = new File(directoryToDelete, "ChildFile.txt");
		childFile.createNewFile();
		File childDirectory = new File(directoryToDelete, "ChildDirectory");
		FileUtils.forceMkdir(childDirectory);

		FileEventOrderListener orderListener = new FileEventOrderListener(directoryToDelete);
		testObserver.addFileObserverListener(orderListener);
		testObserver.start();

		FileUtils.deleteQuietly(directoryToDelete);

		Thread.sleep(WAIT);

		assertTrue(validateOrder(expectedOrder, orderListener.getRealOrder()));

		testObserver.removeFileObserverListener(orderListener);
		testObserver.stop();
	}

	private static boolean validateOrder(List<EventCheck> expected, List<EventCheck> real) {
		if (expected.size() != real.size())
			return false;
		for (int i = 0; i < expected.size(); i++) {
			if (!expected.get(i).equals(real.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsOnly(List<EventCheck> eventChecks, EventCheck sample) {
		for (EventCheck check : eventChecks) {
			if (!check.equals(sample))
				return false;
		}
		return true;
	}

	private static File getTestDirectoryRoot() {
		return Paths.get(FileUtils.getTempDirectoryPath(), "Hive2Hive Test").toFile();
	}

	private class EventCheck {

		private final Event event;
		private final Relation relation;

		private EventCheck(Event event, Relation relation) {
			this.event = event;
			this.relation = relation;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof EventCheck)) {
				return false;
			}
			final EventCheck other = (EventCheck) obj;
			if (this.event != other.event) {
				return false;
			}
			if (this.relation != other.relation) {
				return false;
			}
			return true;
		}
	}

	private final class FileEventOrderListener implements IFileObserverListener {
		private final List<EventCheck> realOrder;

		public List<EventCheck> getRealOrder() {
			return realOrder;
		}

		private final String absoluteFilePath;
		private final String absoluteParentFilePath;

		public FileEventOrderListener(File relativeFile) {
			this.absoluteFilePath = relativeFile.getAbsolutePath();
			this.absoluteParentFilePath = relativeFile.getParentFile().getAbsolutePath();
			this.realOrder = new ArrayList<EventCheck>();
		}

		public void onStop(FileAlterationObserver observer) {
		}

		public void onStart(FileAlterationObserver observer) {
		}

		public void onFileDelete(File file) {
			realOrder.add(new EventCheck(Event.FILE_DELETED, relate(file)));
		}

		public void onFileCreate(File file) {
			realOrder.add(new EventCheck(Event.FILE_CREATED, relate(file)));
		}

		public void onFileChange(File file) {
			realOrder.add(new EventCheck(Event.FILE_CHANGED, relate(file)));
		}

		public void onDirectoryDelete(File directory) {
			realOrder.add(new EventCheck(Event.DIRECTORY_DELETED, relate(directory)));
		}

		public void onDirectoryCreate(File directory) {
			realOrder.add(new EventCheck(Event.DIRECTORY_CREATED, relate(directory)));
		}

		public void onDirectoryChange(File directory) {
			realOrder.add(new EventCheck(Event.DIRECTORY_CHANGED, relate(directory)));
		}

		private Relation relate(File eventFile) {
			if (absoluteParentFilePath.equals(eventFile.getAbsolutePath())) {
				return Relation.PARENT;
			} else if (absoluteFilePath.equals(eventFile.getAbsolutePath())) {
				return Relation.SELF;
			} else if (eventFile.getParentFile().getAbsolutePath().equals(absoluteFilePath)) {
				return Relation.CHILD;
			} else {
				throw new IllegalArgumentException(String.format("%s EventFile: %s, RelativeFile: %s",
						"Relation cannot be evaluated.", eventFile.toPath(), absoluteFilePath));
			}
		}
	}
}
