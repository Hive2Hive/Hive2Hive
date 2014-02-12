package org.hive2hive.core.processes;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.H2HSession;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.decorators.AsyncResultComponent;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;
import org.hive2hive.core.processes.implementations.common.File2MetaFileComponent;
import org.hive2hive.core.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.core.processes.implementations.common.PutMetaDocumentStep;
import org.hive2hive.core.processes.implementations.common.PutUserLocationsStep;
import org.hive2hive.core.processes.implementations.common.userprofiletask.GetUserProfileTaskStep;
import org.hive2hive.core.processes.implementations.context.AddFileProcessContext;
import org.hive2hive.core.processes.implementations.context.DeleteFileProcessContext;
import org.hive2hive.core.processes.implementations.context.DownloadFileContext;
import org.hive2hive.core.processes.implementations.context.LoginProcessContext;
import org.hive2hive.core.processes.implementations.context.LogoutProcessContext;
import org.hive2hive.core.processes.implementations.context.MoveFileProcessContext;
import org.hive2hive.core.processes.implementations.context.NotifyProcessContext;
import org.hive2hive.core.processes.implementations.context.RecoverFileContext;
import org.hive2hive.core.processes.implementations.context.RegisterProcessContext;
import org.hive2hive.core.processes.implementations.context.ShareProcessContext;
import org.hive2hive.core.processes.implementations.context.UpdateFileProcessContext;
import org.hive2hive.core.processes.implementations.context.UserProfileTaskContext;
import org.hive2hive.core.processes.implementations.context.interfaces.IConsumeNotificationFactory;
import org.hive2hive.core.processes.implementations.files.add.AddIndexToUserProfileStep;
import org.hive2hive.core.processes.implementations.files.add.CreateMetaDocumentStep;
import org.hive2hive.core.processes.implementations.files.add.PrepareNotificationStep;
import org.hive2hive.core.processes.implementations.files.add.PutChunksStep;
import org.hive2hive.core.processes.implementations.files.delete.DeleteChunksProcess;
import org.hive2hive.core.processes.implementations.files.delete.DeleteFileOnDiskStep;
import org.hive2hive.core.processes.implementations.files.delete.DeleteFromUserProfileStep;
import org.hive2hive.core.processes.implementations.files.delete.DeleteMetaDocumentStep;
import org.hive2hive.core.processes.implementations.files.delete.PrepareDeleteNotificationStep;
import org.hive2hive.core.processes.implementations.files.download.FindInUserProfileStep;
import org.hive2hive.core.processes.implementations.files.list.GetFileListStep;
import org.hive2hive.core.processes.implementations.files.move.MoveOnDiskStep;
import org.hive2hive.core.processes.implementations.files.move.RelinkUserProfileStep;
import org.hive2hive.core.processes.implementations.files.recover.IVersionSelector;
import org.hive2hive.core.processes.implementations.files.recover.SelectVersionStep;
import org.hive2hive.core.processes.implementations.files.update.CreateNewVersionStep;
import org.hive2hive.core.processes.implementations.files.update.DeleteChunksStep;
import org.hive2hive.core.processes.implementations.files.update.UpdateMD5inUserProfileStep;
import org.hive2hive.core.processes.implementations.login.ContactOtherClientsStep;
import org.hive2hive.core.processes.implementations.login.GetUserProfileStep;
import org.hive2hive.core.processes.implementations.login.SessionCreationStep;
import org.hive2hive.core.processes.implementations.login.SessionParameters;
import org.hive2hive.core.processes.implementations.login.SynchronizeFilesStep;
import org.hive2hive.core.processes.implementations.logout.RemoveOwnLocationsStep;
import org.hive2hive.core.processes.implementations.logout.WritePersistentStep;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;
import org.hive2hive.core.processes.implementations.notify.GetAllLocationsStep;
import org.hive2hive.core.processes.implementations.notify.GetPublicKeysStep;
import org.hive2hive.core.processes.implementations.notify.PutAllUserProfileTasksStep;
import org.hive2hive.core.processes.implementations.notify.RemoveUnreachableStep;
import org.hive2hive.core.processes.implementations.notify.SendNotificationsMessageStep;
import org.hive2hive.core.processes.implementations.notify.VerifyNotificationFactoryStep;
import org.hive2hive.core.processes.implementations.register.AssureUserInexistentStep;
import org.hive2hive.core.processes.implementations.register.PutPublicKeyStep;
import org.hive2hive.core.processes.implementations.register.PutUserProfileStep;
import org.hive2hive.core.processes.implementations.share.PrepareNotificationsStep;
import org.hive2hive.core.processes.implementations.share.UpdateMetaFolderStep;
import org.hive2hive.core.processes.implementations.share.UpdateUserProfileStep;
import org.hive2hive.core.processes.implementations.share.VerifyFriendId;
import org.hive2hive.core.processes.implementations.userprofiletask.HandleUserProfileTaskStep;
import org.hive2hive.core.security.UserCredentials;

/**
 * Factory class for the creation of specific process components and composites that represent basic
 * operations of the Hive2Hive project.
 * 
 * @author Christian, Nico
 * 
 */
public final class ProcessFactory {

	private static ProcessFactory instance;

	public static ProcessFactory instance() {
		if (instance == null)
			instance = new ProcessFactory();
		return instance;
	}

	private ProcessFactory() {
		// singleton
	}

	/**
	 * Creates and returns a registration process.
	 * 
	 * @param credentials The credentials of the user to be registered.
	 * @param networkManager The network manager / node on which the registration operations should be
	 *            executed.
	 * @return A registration process.
	 * @throws NoPeerConnectionException
	 */
	public IProcessComponent createRegisterProcess(UserCredentials credentials, NetworkManager networkManager)
			throws NoPeerConnectionException {
		UserProfile profile = new UserProfile(credentials.getUserId());
		DataManager dataManager = networkManager.getDataManager();
		RegisterProcessContext context = new RegisterProcessContext(profile);

		// process composition
		SequentialProcess process = new SequentialProcess();

		process.add(new AssureUserInexistentStep(credentials.getUserId(), context, dataManager));
		process.add(new AsyncComponent(new PutUserProfileStep(credentials, profile, dataManager)));
		process.add(new AsyncComponent(new PutUserLocationsStep(context, context, dataManager)));
		process.add(new AsyncComponent(new PutPublicKeyStep(profile, dataManager)));

		return process;
	}

	/**
	 * Creates and returns a login process.
	 * 
	 * @param credentials The credentials of the user to be logged in.
	 * @param params The session parameters that shall be used.
	 * @param networkManager The network manager / node on which the login operations should be executed.
	 * @return A login process.
	 * @throws NoPeerConnectionException
	 */
	public ProcessComponent createLoginProcess(UserCredentials credentials, SessionParameters params,
			NetworkManager networkManager) throws NoPeerConnectionException {
		DataManager dataManager = networkManager.getDataManager();
		LoginProcessContext context = new LoginProcessContext();

		// process composition
		SequentialProcess process = new SequentialProcess();

		process.add(new GetUserProfileStep(credentials, context, dataManager));
		process.add(new SessionCreationStep(params, context, networkManager));
		process.add(new GetUserLocationsStep(credentials.getUserId(), context, networkManager
				.getDataManager()));
		process.add(new ContactOtherClientsStep(context, networkManager));
		process.add(new PutUserLocationsStep(context, context, dataManager));
		process.add(new SynchronizeFilesStep(context, networkManager));

		return process;
	}

	public ProcessComponent createUserProfileTaskStep(NetworkManager networkManager) {
		SequentialProcess process = new SequentialProcess();
		UserProfileTaskContext context = new UserProfileTaskContext();
		process.add(new GetUserProfileTaskStep(context, networkManager));
		// Note: this step will add the next steps since it depends on the get result
		process.add(new HandleUserProfileTaskStep(context, networkManager));

		return process;
	}

	/**
	 * Creates and returns a logout process.
	 * 
	 * @param networkManager The network manager / node on which the logout operations should be executed.
	 * @return A logout process.
	 * @throws NoPeerConnectionException
	 * @throws NoSessionException
	 */
	public IProcessComponent createLogoutProcess(NetworkManager networkManager)
			throws NoPeerConnectionException, NoSessionException {
		DataManager dataManager = networkManager.getDataManager();
		H2HSession session = networkManager.getSession();
		LogoutProcessContext context = new LogoutProcessContext(session);

		// process composition
		SequentialProcess process = new SequentialProcess();

		process.add(new GetUserLocationsStep(session.getCredentials().getUserId(), context, dataManager));
		process.add(new RemoveOwnLocationsStep(context, networkManager));
		process.add(new WritePersistentStep(session.getRoot()));

		// TODO to be implemented:
//		// stop all running processes
//		ProcessManager.getInstance().stopAll("Logout stopped all processes.");
//		// quit the session
//		networkManager.setSession(null);
		
		return process;
	}

	/**
	 * Process to create a new file. Note that this is only applicable for a single file, not a whole file
	 * tree.
	 */
	public ProcessComponent createNewFileProcess(File file, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		DataManager dataManager = networkManager.getDataManager();
		AddFileProcessContext context = new AddFileProcessContext(file);

		H2HSession session = networkManager.getSession();

		SequentialProcess process = new SequentialProcess();
		process.add(new AddIndexToUserProfileStep(context, session.getProfileManager(), session.getRoot()));
		process.add(new PutChunksStep(context, dataManager, session.getFileConfiguration()));
		process.add(new CreateMetaDocumentStep(context, networkManager.getUserId()));
		process.add(new PutMetaDocumentStep(context, context, dataManager));
		process.add(new PrepareNotificationStep(context));
		process.add(createNotificationProcess(context, networkManager));

		return process;
	}

	public ProcessComponent createUpdateFileProcess(File file, NetworkManager networkManager)
			throws NoSessionException, IllegalArgumentException, NoPeerConnectionException {
		if (!file.isFile()) {
			throw new IllegalArgumentException("A folder can have one version only");
		}

		DataManager dataManager = networkManager.getDataManager();
		UpdateFileProcessContext context = new UpdateFileProcessContext(file);

		H2HSession session = networkManager.getSession();

		SequentialProcess process = new SequentialProcess();
		process.add(new File2MetaFileComponent(file, context, context, networkManager));
		process.add(new PutChunksStep(context, dataManager, session.getFileConfiguration()));
		process.add(new CreateNewVersionStep(context, session.getFileConfiguration()));
		process.add(new PutMetaDocumentStep(context, context, dataManager));
		process.add(new UpdateMD5inUserProfileStep(context, session.getProfileManager()));

		// TODO: cleanup can be made async because user operation does not depend on it
		process.add(new DeleteChunksStep(context, dataManager));
		process.add(new PrepareNotificationStep(context));
		process.add(createNotificationProcess(context, networkManager));

		return process;
	}

	public ProcessComponent createDownloadFileProcess(PublicKey fileKey, NetworkManager networkManager)
			throws NoSessionException {
		return createDownloadFileProcess(fileKey, DownloadFileContext.NEWEST_VERSION_INDEX, null,
				networkManager);
	}

	/**
	 * Process for downloading with some extra parameters. This can for example be used to restore a file. The
	 * version and the filename are only effective for files, not for folders.
	 */
	public ProcessComponent createDownloadFileProcess(PublicKey fileKey, int versionToDownload,
			File destination, NetworkManager networkManager) throws NoSessionException {
		// precondition: session is existent
		networkManager.getSession();

		SequentialProcess process = new SequentialProcess();
		DownloadFileContext context = new DownloadFileContext(fileKey, destination, versionToDownload);
		process.add(new FindInUserProfileStep(context, networkManager));

		return process;
	}

	/**
	 * Deletes the specified file. Note that this is only valid for a single file or an empty folder
	 * (non-recursive)
	 */
	public ProcessComponent createDeleteFileProcess(File file, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		DataManager dataManager = networkManager.getDataManager();
		DeleteFileProcessContext context = new DeleteFileProcessContext(file.isDirectory());

		// process composition
		SequentialProcess process = new SequentialProcess();

		process.add(new DeleteFileOnDiskStep(file)); // TODO make asynchronous
		process.add(new File2MetaFileComponent(file, context, context, networkManager));
		process.add(new DeleteChunksProcess(context, dataManager));
		process.add(new DeleteMetaDocumentStep(context, dataManager));
		process.add(new DeleteFromUserProfileStep(context, networkManager));

		process.add(new PrepareDeleteNotificationStep(context));
		process.add(createNotificationProcess(context, networkManager));

		return process;
	}

	public ProcessComponent createMoveFileProcess(File source, File destination, NetworkManager networkManager)
			throws NoSessionException, NoPeerConnectionException {
		// make some checks here, thus it's easier in the steps
		MoveFileProcessContext context = new MoveFileProcessContext(source, destination,
				networkManager.getUserId());

		SequentialProcess process = new SequentialProcess();
		process.add(new MoveOnDiskStep(context, networkManager));
		process.add(new RelinkUserProfileStep(context, networkManager));
		process.add(createNotificationProcess(context.getMoveNotificationContext(), networkManager));
		process.add(createNotificationProcess(context.getDeleteNotificationContext(), networkManager));
		process.add(createNotificationProcess(context.getAddNotificationContext(), networkManager));

		return process;
	}

	public ProcessComponent createRecoverFileProcess(File file, IVersionSelector selector,
			NetworkManager networkManager) throws FileNotFoundException, IllegalArgumentException,
			NoSessionException, NoPeerConnectionException {
		// do some verifications
		if (file.isDirectory()) {
			throw new IllegalArgumentException("A foler has only one version");
		} else if (!file.exists()) {
			throw new FileNotFoundException("File does not exist");
		}

		RecoverFileContext context = new RecoverFileContext(file);
		SequentialProcess process = new SequentialProcess();
		process.add(new File2MetaFileComponent(file, context, context, networkManager));
		process.add(new SelectVersionStep(context, selector, networkManager));

		// return new AsyncComponent(process);
		return process;
	}

	/**
	 * Creates and returns a file list process.
	 * 
	 * @param networkManager The network manager / node on which the file list operations should be executed.
	 * @return A file list process.
	 */
	public IResultProcessComponent<List<Path>> createFileListProcess(NetworkManager networkManager) {
		GetFileListStep listStep = new GetFileListStep(networkManager);
		return new AsyncResultComponent<List<Path>>(listStep);
	}

	public ProcessComponent createNotificationProcess(final BaseNotificationMessageFactory messageFactory,
			final Set<String> usersToNotify, NetworkManager networkManager) throws IllegalArgumentException,
			NoPeerConnectionException {
		// create a context here to provide the necessary data
		IConsumeNotificationFactory context = new IConsumeNotificationFactory() {

			@Override
			public Set<String> consumeUsersToNotify() {
				return usersToNotify;
			}

			@Override
			public BaseNotificationMessageFactory consumeMessageFactory() {
				return messageFactory;
			}
		};
		return createNotificationProcess(context, networkManager);
	}

	private ProcessComponent createNotificationProcess(IConsumeNotificationFactory providerContext,
			NetworkManager networkManager) throws IllegalArgumentException, NoPeerConnectionException {
		NotifyProcessContext context = new NotifyProcessContext(providerContext);

		SequentialProcess process = new SequentialProcess();
		process.add(new VerifyNotificationFactoryStep(context, networkManager.getUserId()));
		process.add(new GetPublicKeysStep(context, networkManager));
		process.add(new PutAllUserProfileTasksStep(context, networkManager));
		process.add(new GetAllLocationsStep(context, networkManager.getDataManager()));
		process.add(new SendNotificationsMessageStep(context, networkManager));
		// cleanup my own locations
		process.add(new GetUserLocationsStep(networkManager.getUserId(), context, networkManager
				.getDataManager()));
		process.add(new RemoveUnreachableStep(context, networkManager));

		return process;
	}

	public ProcessComponent createShareProcess(File folder, UserPermission permission,
			NetworkManager networkManager) throws IllegalFileLocation, IllegalArgumentException,
			NoSessionException, NoPeerConnectionException {
		// verify
		if (!folder.isDirectory())
			throw new IllegalArgumentException("File has to be a folder.");
		if (!folder.exists())
			throw new IllegalFileLocation("Folder does not exist.");

		H2HSession session = networkManager.getSession();
		Path root = session.getRoot();

		// folder must be in the given root directory
		if (!folder.toPath().toString().startsWith(root.toString()))
			throw new IllegalFileLocation("Folder must be in root of the H2H directory.");

		// sharing root folder is not allowed
		if (folder.toPath().toString().equals(root.toString()))
			throw new IllegalFileLocation("Root folder of the H2H directory can't be shared.");

		ShareProcessContext context = new ShareProcessContext(folder, permission);

		SequentialProcess process = new SequentialProcess();
		process.add(new VerifyFriendId(networkManager, permission.getUserId()));
		process.add(new File2MetaFileComponent(folder, context, context, networkManager));
		process.add(new UpdateMetaFolderStep(context, networkManager.getDataManager()));
		process.add(new UpdateUserProfileStep(context, networkManager.getSession().getProfileManager()));
		process.add(new PrepareNotificationsStep(context));
		process.add(createNotificationProcess(context, networkManager));

		return process;
	}
}
