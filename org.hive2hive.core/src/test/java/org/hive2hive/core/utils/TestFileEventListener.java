package org.hive2hive.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.engio.mbassy.listener.Handler;

import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;

/**
 * Helper class to check whether file events have been triggered
 * 
 * @author Nico
 *
 */
public class TestFileEventListener implements IFileEventListener {

	private List<IFileAddEvent> add = new ArrayList<>();
	private List<IFileUpdateEvent> update = new ArrayList<>();
	private List<IFileDeleteEvent> delete = new ArrayList<>();
	private List<IFileMoveEvent> move = new ArrayList<>();
	private List<IFileShareEvent> share = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private <T> T getByFile(List<? extends IFileEvent> events, File file) {
		for (IFileEvent event : events) {
			if (event.getFile().equals(file)) {
				return (T) event;
			}
		}
		return null;
	}

	@Override
	@Handler
	public void onFileAdd(IFileAddEvent fileEvent) {
		add.add(fileEvent);
	}

	public IFileAddEvent getAdded(File file) {
		return getByFile(add, file);
	}

	@Override
	@Handler
	public void onFileUpdate(IFileUpdateEvent fileEvent) {
		update.add(fileEvent);
	}

	public IFileUpdateEvent getUpdated(File file) {
		return getByFile(update, file);
	}

	@Override
	@Handler
	public void onFileDelete(IFileDeleteEvent fileEvent) {
		delete.add(fileEvent);
	}

	public IFileDeleteEvent getDeleted(File file) {
		return getByFile(delete, file);
	}

	@Override
	@Handler
	public void onFileMove(IFileMoveEvent fileEvent) {
		move.add(fileEvent);
	}

	public IFileMoveEvent getMoved(File file) {
		return getByFile(move, file);
	}

	@Override
	@Handler
	public void onFileShare(IFileShareEvent fileEvent) {
		share.add(fileEvent);
	}

	public IFileShareEvent getShared(File file) {
		return getByFile(share, file);
	}

}
