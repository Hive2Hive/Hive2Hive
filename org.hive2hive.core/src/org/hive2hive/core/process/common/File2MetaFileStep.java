package org.hive2hive.core.process.common;

import java.io.File;

import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.file.FileManager;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.get.GetMetaDocumentStep;
import org.hive2hive.core.process.common.get.GetUserProfileStep;
import org.hive2hive.core.process.context.IGetMetaContext;

/**
 * Finds a given file in the user profile and gets the appropriate meta data. Note that a
 * {@link GetUserProfileStep} must be run before this step is run.
 * 
 * @author Nico
 * 
 */
public class File2MetaFileStep extends ProcessStep {

	private final File file;
	private final IGetMetaContext metaContext;
	private final FileManager fileManager;
	private final ProcessStep nextStep;
	private FileTreeNode fileNode;
	private UserProfileManager profileManager;

	public File2MetaFileStep(File file, UserProfileManager profileManager, FileManager fileManager,
			IGetMetaContext metaContext, ProcessStep nextStep) {
		this(file, null, profileManager, fileManager, metaContext, nextStep);
	}

	public File2MetaFileStep(FileTreeNode fileNode, UserProfileManager profileManager,
			FileManager fileManager, IGetMetaContext metaContext, ProcessStep nextStep) {
		this(null, fileNode, profileManager, fileManager, metaContext, nextStep);
	}

	private File2MetaFileStep(File file, FileTreeNode fileNode, UserProfileManager profileManager,
			FileManager fileManager, IGetMetaContext metaContext, ProcessStep nextStep) {
		this.file = file;
		this.profileManager = profileManager;
		this.fileNode = fileNode;
		this.fileManager = fileManager;
		this.metaContext = metaContext;
		this.nextStep = nextStep;
	}

	@Override
	public void start() {
		try {
			UserProfile profile = profileManager.getUserProfile(getProcess());

			if (fileNode == null) {
				fileNode = profile.getFileByPath(file, fileManager);
				if (fileNode == null) {
					getProcess().stop(
							"File does not exist in user profile. You might consider uploading a new file");
					return;
				}
			}

			// get the appropriate meta document and then update it
			GetMetaDocumentStep getMetaStep = new GetMetaDocumentStep(fileNode.getKeyPair(), nextStep,
					metaContext);
			getProcess().setNextStep(getMetaStep);
		} catch (GetFailedException e) {
			getProcess().stop("Could not get the user profile");
		}
	}

	@Override
	public void rollBack() {
		// nothing to do
		getProcess().nextRollBackStep();
	}
}
