package org.hive2hive.core.api.interfaces;

import org.apache.commons.io.monitor.FileAlterationListener;

/**
 * Interface for any file observer listener. Internally uses the Apache Common IO
 * {@link FileAlterationListener} interface.
 * 
 * @author Christian
 * 
 */
public interface IFileObserverListener extends FileAlterationListener {

	// void onObservationStarted();
	//
	// void onObservationStopped();
	//
	// void onFileAdded();
	//
	// void onFileModified();
	//
	// void onFileDeleted();
	//
	// void onDirectoryAdded();
	//
	// void onDirectoryModified();
	//
	// void onDirectoryDeleted();
}
