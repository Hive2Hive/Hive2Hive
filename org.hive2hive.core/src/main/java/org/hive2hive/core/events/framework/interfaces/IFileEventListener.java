package org.hive2hive.core.events.framework.interfaces;

import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDownloadEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;

import net.engio.mbassy.listener.Handler;

public interface IFileEventListener {
	
	@Handler
	void onFileDelete(IFileDeleteEvent fileEvent);
	
	@Handler
	void onFileDownload(IFileDownloadEvent fileEvent);
	
	@Handler
	void onFileMove(IFileMoveEvent fileEvent);
}
