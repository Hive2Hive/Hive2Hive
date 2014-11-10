package org.hive2hive.core.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.events.framework.interfaces.IFileEventListener;
import org.hive2hive.core.events.framework.interfaces.file.IFileAddEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileDeleteEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileMoveEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileShareEvent;
import org.hive2hive.core.events.framework.interfaces.file.IFileUpdateEvent;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.network.NetworkManager;
import org.junit.Assert;

/**
 * Helper class to check whether file events have been triggered
 * 
 * @author Nico
 *
 */
public class TestFileEventListener implements IFileEventListener {

	private final NetworkManager networkManager;

	private final List<IFileAddEvent> add = new ArrayList<>();
	private final List<IFileUpdateEvent> update = new ArrayList<>();
	private final List<IFileDeleteEvent> delete = new ArrayList<>();
	private final List<IFileMoveEvent> move = new ArrayList<>();
	private final List<IFileShareEvent> share = new ArrayList<>();

	/**
	 * Just collects file events and allows to check them
	 */
	public TestFileEventListener() {
		this(null);
	}

	/**
	 * @param networkManager if set to null, no actions will be made. If it's not null, events will
	 *            automatically be handled (e.g. files are downloaded, moved, updated, ...)
	 */
	public TestFileEventListener(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

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
		if (networkManager != null) {
			try {
				UseCaseTestUtil.downloadFile(networkManager, fileEvent.getFile());
			} catch (NoSessionException | GetFailedException | NoPeerConnectionException e) {
				Assert.fail("Cannot download the added file. Reason: " + e.getMessage());
			}
		}
	}

	public IFileAddEvent getAdded(File file) {
		return getByFile(add, file);
	}

	@Override
	@Handler
	public void onFileUpdate(IFileUpdateEvent fileEvent) {
		update.add(fileEvent);

		if (networkManager != null) {
			try {
				UseCaseTestUtil.downloadFile(networkManager, fileEvent.getFile());
			} catch (NoSessionException | GetFailedException | NoPeerConnectionException e) {
				Assert.fail("Cannot download the updated file. Reason: " + e.getMessage());
			}
		}
	}

	public IFileUpdateEvent getUpdated(File file) {
		return getByFile(update, file);
	}

	@Override
	@Handler
	public void onFileDelete(IFileDeleteEvent fileEvent) {
		delete.add(fileEvent);
		if (networkManager != null) {
			fileEvent.getFile().delete();
		}
	}

	public IFileDeleteEvent getDeleted(File file) {
		return getByFile(delete, file);
	}

	@Override
	@Handler
	public void onFileMove(IFileMoveEvent fileEvent) {
		move.add(fileEvent);
		if (networkManager != null) {
			try {
				FileUtils.moveFile(fileEvent.getSrcFile(), fileEvent.getDstFile());
			} catch (IOException e) {
				Assert.fail("Cannot move the file. Reason: " + e.getMessage());

			}
		}
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
