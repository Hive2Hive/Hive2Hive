package org.hive2hive.processes;

import java.io.File;
import java.nio.file.Path;

import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.login.SessionParameters;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processes.framework.concretes.SequentialProcess;
import org.hive2hive.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.processes.implementations.common.PutMetaDocumentStep;
import org.hive2hive.processes.implementations.common.PutUserLocationsStep;
import org.hive2hive.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.processes.implementations.context.LoginProcessContext;
import org.hive2hive.processes.implementations.context.RegisterProcessContext;
import org.hive2hive.processes.implementations.files.add.CreateMetaDocumentStep;
import org.hive2hive.processes.implementations.files.add.GetParentMetaStep;
import org.hive2hive.processes.implementations.files.add.PutChunksStep;
import org.hive2hive.processes.implementations.files.add.UpdateParentMetaStep;
import org.hive2hive.processes.implementations.files.add.UpdateUserProfileStep;
import org.hive2hive.processes.implementations.login.ContactOtherClientsStep;
import org.hive2hive.processes.implementations.login.GetUserProfileStep;
import org.hive2hive.processes.implementations.login.SessionCreationStep;
import org.hive2hive.processes.implementations.login.SynchronizeFilesStep;
import org.hive2hive.processes.implementations.register.AssureUserInexistentStep;
import org.hive2hive.processes.implementations.register.PutPublicKeyStep;
import org.hive2hive.processes.implementations.register.PutUserProfileStep;

public final class ProcessFactory {

	private static ProcessFactory instance;

	public static ProcessFactory instance() {
		if (instance == null)
			instance = new ProcessFactory();
		return instance;
	}

	private ProcessFactory() {
	}

	public IProcessComponent createRegisterProcess(UserCredentials credentials, NetworkManager networkManager) {
		UserProfile profile = new UserProfile(credentials.getUserId());
		RegisterProcessContext context = new RegisterProcessContext(profile);

		// process composition
		SequentialProcess process = new SequentialProcess();

		process.add(new AssureUserInexistentStep(credentials.getUserId(), context, networkManager));
		process.add(new PutUserProfileStep(credentials, profile, networkManager));
		process.add(new PutUserLocationsStep(context, context, networkManager));
		process.add(new PutPublicKeyStep(profile, networkManager));
		// process.add(new AsyncComponent(new PutUserProfileStep(credentials, profile, networkManager)));
		// process.add(new AsyncComponent(new PutUserLocationsStep(context, context, networkManager)));
		// process.add(new AsyncComponent(new PutPublicKeyStep(profile, networkManager)));

		// AsyncComponent registerProcess = new AsyncComponent(process);

		return process;
	}

	public IProcessComponent createLoginProcess(UserCredentials credentials, SessionParameters params,
			NetworkManager networkManager) {

		LoginProcessContext context = new LoginProcessContext();

		// process composition
		SequentialProcess process = new SequentialProcess();

		process.add(new GetUserProfileStep(credentials, context, networkManager));
		process.add(new SessionCreationStep(params, context, networkManager));
		process.add(new GetUserLocationsStep(credentials.getUserId(), context, networkManager));
		process.add(new ContactOtherClientsStep(context, networkManager));
		process.add(new PutUserLocationsStep(context, context, networkManager));
		process.add(new SynchronizeFilesStep(context));

		// AsyncComponent loginProcess = new AsyncComponent(process);

		return process;
	}

	public IProcessComponent createNewFileProcess(File file, NetworkManager networkManager)
			throws NoSessionException {
		Path root = networkManager.getSession().getFileManager().getRoot();
		boolean inRoot = root.equals(file.toPath().getParent());

		AddFileProcessContext context = new AddFileProcessContext(file, inRoot, networkManager.getSession());

		SequentialProcess process = new SequentialProcess();
		process.add(new GetParentMetaStep(context, networkManager));
		process.add(new PutChunksStep(context, networkManager));
		process.add(new CreateMetaDocumentStep(context));
		process.add(new PutMetaDocumentStep(context, context, networkManager));

		if (!inRoot) {
			// need to update the parent if necessary
			process.add(new UpdateParentMetaStep(context, networkManager));
		}

		process.add(new UpdateUserProfileStep(context));
		// TODO notify others

		// AsyncComponent addFileProcess = new AsyncComponent(process);
		return process;
	}
}
