package org.hive2hive.core.process.recover;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFile;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.download.DownloadFileProcess;
import org.hive2hive.core.process.listener.IProcessListener;

/**
 * Asks the user which version to restore and initiates the restore steps
 * 
 * @author Nico
 * 
 */
public class SelectVersionStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(SelectVersionStep.class);

	@Override
	public void start() {
		RecoverFileProcessContext context = (RecoverFileProcessContext) getProcess().getContext();

		MetaDocument metaDocument = context.getMetaDocument();
		if (metaDocument == null) {
			getProcess().stop("Meta document not found");
			return;
		} else if (!(metaDocument instanceof MetaFile)) {
			getProcess().stop("Meta document is not a meta file");
			return;
		}

		MetaFile metaFile = (MetaFile) metaDocument;

		// cast the versions to the public interface
		List<IFileVersion> versions = new ArrayList<IFileVersion>();
		for (FileVersion version : metaFile.getVersions()) {
			if (metaFile.getNewestVersion().equals(version)) {
				// skip newest version since it's not worth to restore it
				continue;
			}

			versions.add(version);
		}

		logger.debug("Start with the selection of the version by the user. He has choice between "
				+ versions.size() + " versions");
		IFileVersion selected = context.getVersionSelector().selectVersion(versions);
		if (selected == null) {
			getProcess().stop("Selected file version is null");
			return;
		}

		// find the selected version
		for (FileVersion version : metaFile.getVersions()) {
			if (version.getIndex() == selected.getIndex()) {
				context.setSelectedFileVersion(version);
				break;
			}
		}

		// check if the developer returned an invalid index
		if (context.getSelectedFileVersion() == null) {
			getProcess().stop("Invalid version index selected");
			return;
		}

		logger.debug("Selected version " + selected.getIndex() + " where "
				+ metaFile.getNewestVersion().getIndex() + " is newest");

		// 1. download the file with new name <filename>_<date>
		// 2. add the file with an AddFileProcess (which also notifies other clients)
		try {
			// find the node at the user profile
			UserProfileManager profileManager = getNetworkManager().getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), false);
			FileTreeNode selectedNode = userProfile.getFileById(metaFile.getId());
			if (selectedNode == null) {
				throw new Hive2HiveException("File node not found");
			}

			// generate a new file name indicating that the file is restored
			Date versionDate = new Date(selected.getDate());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
			String newFileName = context.getFile().getName() + "_" + sdf.format(versionDate);
			logger.debug("Starting to download the restored file under the name '" + newFileName + "'");

			// start the process to download the files
			DownloadFileProcess process = new DownloadFileProcess(selectedNode, getNetworkManager(),
					selected.getIndex(), newFileName);
			process.addListener(new StartAddRestoredFileListener(process.getContext().getDestination()));
			process.start();
		} catch (Hive2HiveException e) {
			getProcess().stop(e);
		}

	}

	@Override
	public void rollBack() {
		// This process did not change any persistent objects
		getProcess().nextRollBackStep();
	}

	/**
	 * Listener that is called as soon the download of the restored version is done
	 * 
	 * @author Nico
	 * 
	 */
	private class StartAddRestoredFileListener implements IProcessListener {

		private Path path;

		public StartAddRestoredFileListener(Path path) {
			this.path = path;
		}

		@Override
		public void onSuccess() {
			logger.debug("Downloading of the restored version finished. Continue with adding it as an own file");
			// try {
			// IProcess process = new NewFileProcess(path.toFile(), getNetworkManager());
			// process.addListener(new FinalizeRestoreListener());
			// process.start();
			// } catch (Hive2HiveException e) {
			// getProcess().stop(e);
			// }
		}

		@Override
		public void onFail(Exception exception) {
			logger.error("Could not download the restored version");
			getProcess().stop(exception);
		}
	}

	/**
	 * Listener that is called as soon as the restored file is uploaded to the DHT
	 * 
	 * @author Nico
	 * 
	 */
	private class FinalizeRestoreListener implements IProcessListener {

		@Override
		public void onSuccess() {
			logger.debug("Successfully restored the file version");
			getProcess().setNextStep(null);
		}

		@Override
		public void onFail(Exception exception) {
			logger.error("Could not upload the restored file to the DHT");
			getProcess().stop(exception);
		}
	}
}
