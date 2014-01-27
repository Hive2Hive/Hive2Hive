package org.hive2hive.processes.implementations.files.add;

import java.util.HashSet;
import java.util.Set;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.process.upload.UploadNotificationMessageFactory;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.AddFileProcessContext;

/**
 * Provide the needed data for the notification
 * 
 * @author Nico
 * 
 */
public class PrepareNotificationStep extends ProcessStep {

	private AddFileProcessContext context;

	public PrepareNotificationStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		MetaDocument metaDocument = context.consumeMetaDocument();
		UploadNotificationMessageFactory messageFactory = new UploadNotificationMessageFactory(
				metaDocument.getId());
		context.provideMessageFactory(messageFactory);

		if (context.isInRoot()) {
			// file is in root; notify only own client
			Set<String> onlyMe = new HashSet<String>(1);
			onlyMe.add(context.getH2HSession().getCredentials().getUserId());
			context.provideUsersToNotify(onlyMe);
		} else {
			MetaFolder metaFolder = (MetaFolder) context.consumeParentMetaDocument();
			Set<String> userList = metaFolder.getUserList();
			context.provideUsersToNotify(userList);
		}
	}
}
