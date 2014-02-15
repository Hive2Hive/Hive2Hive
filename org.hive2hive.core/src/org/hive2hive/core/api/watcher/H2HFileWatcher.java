package org.hive2hive.core.api.watcher;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class H2HFileWatcher {

	private final File rootDirectory;
	private final FileFilter fileFilter;
	private final IOCase caseSensitivity;
	private final long interval;

	private final FileAlterationObserver rootObserver;
	private final FileAlterationMonitor monitor;

	private H2HFileWatcher(H2HFileWatcherBuilder builder) {
		this.rootDirectory = builder.rootDirectory;
		this.fileFilter = builder.fileFilter;
		this.caseSensitivity = builder.caseSensitivity;
		this.interval = builder.interval;

		rootObserver = new FileAlterationObserver(rootDirectory, fileFilter, caseSensitivity);
		monitor = new FileAlterationMonitor(interval, rootObserver);
	}

	public void start() throws Exception {
		monitor.start();
	}

	public void stop() throws Exception {
		monitor.stop();
	}

	public void addFileListener(FileAlterationListener listener) {
		rootObserver.addListener(listener);
	}

	public void removeFileListener(FileAlterationListener listener) {
		rootObserver.removeListener(listener);
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	public FileFilter getFileFilter() {
		return fileFilter;
	}

	public IOCase getCaseSensitivity() {
		return caseSensitivity;
	}

	public long getInterval() {
		return interval;
	}

	public static class H2HFileWatcherBuilder {

		// required
		private final File rootDirectory;

		// optional
		private FileFilter fileFilter;
		private IOCase caseSensitivity;
		private long interval;

		public H2HFileWatcherBuilder(File rootDirectory) {
			this.rootDirectory = rootDirectory;
		}

		public H2HFileWatcherBuilder setFileFilter(FileFilter fileFilter) {
			this.fileFilter = fileFilter;
			return this;
		}

		public H2HFileWatcherBuilder setCaseSensivity(IOCase caseSensivity) {
			this.caseSensitivity = caseSensivity;
			return this;
		}

		public H2HFileWatcherBuilder setInterval(long interval) {
			this.interval = interval;
			return this;
		}

		public H2HFileWatcher build() {
			return new H2HFileWatcher(this);
		}
	}
}
