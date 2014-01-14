package org.hive2hive.core.test.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.hive2hive.core.file.watcher.H2HFileWatcher;
import org.hive2hive.core.file.watcher.H2HFileWatcher.H2HFileWatcherBuilder;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class H2HFileWatcherTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = H2HFileWatcherTest.class;
		beforeClass();
	}
	
	@Before
	public void createTestDirectory() throws Exception {
		if (Files.exists(getTestDirectoryRoot().toPath(), LinkOption.NOFOLLOW_LINKS)){
			FileUtils.deleteDirectory(getTestDirectoryRoot());
		}
		FileUtils.forceMkdir(getTestDirectoryRoot());
		logger.debug("Test Directory created.");
	}
	
	@After
	public void removeTestDirectory() throws Exception {
		FileUtils.deleteDirectory(getTestDirectoryRoot());
		logger.debug("Test Directory removed.");
	}
	
	@Test
	public void builderTest() {
		
		FileFilter filter = new TestFileFilter();
		long interval = new Random().nextLong();
		
		H2HFileWatcherBuilder watcherBuilder = new H2HFileWatcher.H2HFileWatcherBuilder(getTestDirectoryRoot());
		watcherBuilder.setFileFilter(filter);
		watcherBuilder.setCaseSensivity(IOCase.SYSTEM);
		watcherBuilder.setInterval(interval);
		H2HFileWatcher watcher = watcherBuilder.build();
		
		assertEquals(filter, watcher.getFileFilter());
		assertEquals(IOCase.SYSTEM, watcher.getCaseSensitivity());
		assertTrue(interval == watcher.getInterval());
	}
	
	@Test
	public void listenerTest() throws Exception {
		
		H2HFileWatcher watcher = new H2HFileWatcher.H2HFileWatcherBuilder(getTestDirectoryRoot()).build();
		
		final boolean[] notifiedEvent = new boolean[] {false, false, false, false, false, false, false, false };
		
		FileAlterationListener listener = new FileAlterationListener() {
			
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
		watcher.addFileListener(listener);
		watcher.start();
		
		// trigger all events
		File subDirectory = Paths.get(getTestDirectoryRoot().getAbsolutePath(), "SubFolderTest").toFile();
		FileUtils.forceMkdir(subDirectory);
		File file = new File(getTestDirectoryRoot(), "File.txt");
		file.createNewFile();
		FileUtils.write(file, "write test");
		FileUtils.moveFileToDirectory(file, subDirectory, true);
		FileUtils.deleteQuietly(file);
		FileUtils.deleteQuietly(subDirectory);
		
		for (int i = 0; i < notifiedEvent.length; i++){
			logger.debug(String.format("[%s]: %s", i+1, notifiedEvent[i]));
			assertTrue(notifiedEvent[i]);
		}
	}
	
	public void notificationOrderTest() {
		
	}
	
	private static File getTestDirectoryRoot() {
		return Paths.get(FileUtils.getUserDirectoryPath(), "Hive2Hive Test").toFile();
	}
	
	private class TestFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().contains("test");
		}
		
	}
	
}
