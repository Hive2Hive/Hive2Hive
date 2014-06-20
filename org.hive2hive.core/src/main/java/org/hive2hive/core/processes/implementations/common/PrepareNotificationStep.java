package org.hive2hive.core.processes.implementations.common;

import java.security.PublicKey;
import java.util.HashSet;

import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.implementations.context.interfaces.IPrepareNotificationContext;
import org.hive2hive.core.processes.implementations.files.add.UploadNotificationMessageFactory;

/**
 * Provide the needed data for the notification
 * 
 * @author Nico, Seppi
 */
public class PrepareNotificationStep extends ProcessStep {

	private final IPrepareNotificationContext context;

	public PrepareNotificationStep(IPrepareNotificationContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		// get the recently added index
		Index index = context.consumeIndex();

		// get the users belonging to that index
		HashSet<String> users = new HashSet<String>();
		users.addAll(index.getCalculatedUserList());
		context.provideUsersToNotify(users);

		// prepare the file tree node for sending to other users
		PublicKey parentKey = index.getParent().getFilePublicKey();

		Index indexToSend;
		if (index instanceof FileIndex) {
			FileIndex fileIndex = (FileIndex) index;
			indexToSend = new FileIndex(fileIndex);
		} else if (index instanceof FolderIndex) {
			FolderIndex folderIndex = (FolderIndex) index;
			indexToSend = new FolderIndex(folderIndex);
		} else {
			throw new ProcessExecutionException("Unknown index object");
		}
		// decouple from file tree
		indexToSend.decoupleFromParent();

		UploadNotificationMessageFactory messageFactory = new UploadNotificationMessageFactory(indexToSend, parentKey);
		context.provideMessageFactory(messageFactory);
	}

}
