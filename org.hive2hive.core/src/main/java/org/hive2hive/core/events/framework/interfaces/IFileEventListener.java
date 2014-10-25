package org.hive2hive.core.events.framework.interfaces;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDownloadEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;

public interface IFileEventListener {

	@Handler
	void onFileAdd(IFileAddEvent fileEvent);

	@Handler
	void onFileDelete(IFileDeleteEvent fileEvent);

	@Handler
	void onFileDownload(IFileDownloadEvent fileEvent);

	@Handler
	void onFileMove(IFileMoveEvent fileEvent);

}
