package org.hive2hive.core.processes.files.recover;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.MetaFileSmall;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.processes.ProcessFactory;
import org.hive2hive.core.processes.context.RecoverFileContext;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asks the user which version to restore and initiates the restore steps
 * 
 * @author Nico
 * 
 */
public class SelectVersionStep extends ProcessStep {

	private static final Logger logger = LoggerFactory.getLogger(SelectVersionStep.class);
	private final RecoverFileContext context;
	private final IVersionSelector selector;
	private final NetworkManager networkManager;

	public SelectVersionStep(RecoverFileContext context, IVersionSelector selector, NetworkManager networkManager) {
		this.context = context;
		this.selector = selector;
		this.networkManager = networkManager;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {
		MetaFile metaFile = context.consumeMetaFile();
		if (metaFile == null) {
			throw new ProcessExecutionException("Meta document not found.");
		} else if (!metaFile.isSmall()) {
			throw new ProcessExecutionException("Meta document is not a small meta file.");
		}

		MetaFileSmall metaFileSmall = (MetaFileSmall) metaFile;
		// cast the versions to the public interface
		List<IFileVersion> versions = new ArrayList<IFileVersion>();
		for (FileVersion version : metaFileSmall.getVersions()) {
			if (metaFileSmall.getNewestVersion().equals(version)) {
				// skip newest version since it's not worth to restore it
				continue;
			}

			versions.add(version);
		}

		logger.debug("Start with the selection of the version by the user. The user has choice between {} versions.",
				versions.size());
		IFileVersion selected = selector.selectVersion(versions);
		if (selected == null) {
			throw new ProcessExecutionException("Selected file version is null.");
		}

		// find the selected version
		FileVersion selectedVersion = null;
		for (FileVersion version : metaFileSmall.getVersions()) {
			if (version.getIndex() == selected.getIndex()) {
				selectedVersion = version;
				break;
			}
		}

		// check if the developer returned an invalid index
		if (selectedVersion == null) {
			throw new ProcessExecutionException("Invalid version index selected.");
		}

		logger.debug("Selected version {} where {} is newest.", selected.getIndex(), metaFileSmall.getNewestVersion()
				.getIndex());

		// 1. download the file with new name <filename>_<date>
		// 2. add the file with an AddFileProcess (which also notifies other clients)
		try {
			// find the node at the user profile
			UserProfileManager profileManager = networkManager.getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getID(), false);
			Index selectedNode = userProfile.getFileById(metaFileSmall.getId());
			if (selectedNode == null) {
				throw new Hive2HiveException("File node not found");
			}

			// ask the user for the new file name
			String originalFileName = context.consumeFile().getName();
			String noSuffix = FilenameUtils.removeExtension(originalFileName);
			String extension = FilenameUtils.getExtension(originalFileName);
			String recoveredFileName = selector.getRecoveredFileName(originalFileName, noSuffix, extension);
			if (recoveredFileName == null || originalFileName.equals(recoveredFileName)) {
				// generate a new file name indicating that the file is restored
				logger.warn("Replacing the given file name with a custom file name because it was invalid.");
				Date versionDate = new Date(selected.getDate());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
				recoveredFileName = noSuffix + "-" + sdf.format(versionDate) + "." + extension;
			}

			logger.debug("Starting to download the restored file under the name '{}'.", recoveredFileName);
			File destination = new File(context.consumeFile().getParentFile(), recoveredFileName);

			// add the process to download the file
			ProcessComponent downloadProcess = ProcessFactory.instance().createDownloadFileProcess(
					selectedNode.getFilePublicKey(), selected.getIndex(), destination, networkManager);
			getParent().add(downloadProcess);

			// add the process to upload the file
			ProcessComponent addProcess = ProcessFactory.instance().createNewFileProcess(destination, networkManager);
			getParent().add(addProcess);
		} catch (Hive2HiveException e) {
			throw new ProcessExecutionException(e);
		}

	}
}
