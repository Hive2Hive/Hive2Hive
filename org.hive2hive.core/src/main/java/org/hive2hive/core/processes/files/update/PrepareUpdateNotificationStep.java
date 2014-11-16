package org.hive2hive.core.processes.files.update;

import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.processes.context.UpdateFileProcessContext;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

/**
 * Provide the needed data for the update notification.
 * 
 * @author Nico, Seppi
 */
public class PrepareUpdateNotificationStep extends ProcessStep<Void> {

	private final UpdateFileProcessContext context;

	public PrepareUpdateNotificationStep(UpdateFileProcessContext context) {
		this.setName(getClass().getName());
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// get the recently updated index
		Index index = context.consumeIndex();

		// get the users belonging to that index
		Set<String> users = new HashSet<String>();
		users.addAll(index.getCalculatedUserList());
		context.provideUsersToNotify(users);

		// prepare the file tree node for sending to other users
		Index indexToSend;
		if (index instanceof FileIndex) {
			FileIndex fileIndex = (FileIndex) index;
			indexToSend = new FileIndex(fileIndex);
		} else if (index instanceof FolderIndex) {
			FolderIndex folderIndex = (FolderIndex) index;
			indexToSend = new FolderIndex(folderIndex);
		} else {
			throw new ProcessExecutionException(this, "Unknown index object.");
		}
		// decouple from file tree
		indexToSend.decoupleFromParent();

		UpdateNotificationMessageFactory messageFactory = new UpdateNotificationMessageFactory(indexToSend);
		context.provideMessageFactory(messageFactory);
		
		return null;
	}

}
