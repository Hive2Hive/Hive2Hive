package org.hive2hive.core.process.digest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.network.data.UserProfileManager;
import org.hive2hive.core.process.ProcessStep;

/**
 * Gets the user profile and walks recursively through the file tree to build the digest.
 * 
 * @author Seppi
 */
public class GetDigestProcessStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(GetDigestProcessStep.class);

	@Override
	public void start() {
		logger.debug("Getting digest.");

		try {
			UserProfileManager profileManager = getNetworkManager().getSession().getProfileManager();
			UserProfile userProfile = profileManager.getUserProfile(getProcess().getID(), false);

			FileTreeNode root = userProfile.getRoot();
			List<Path> digest = new ArrayList<Path>();

			walkTrough(root, digest);

			Collections.sort(digest);

			GetDigestContext context = (GetDigestContext) getProcess().getContext();
			context.setDigest(digest);

			// done with all steps
			getProcess().setNextStep(null);
		} catch (NoSessionException | GetFailedException e) {
			getProcess().stop(e);
			return;
		}
	}

	private void walkTrough(FileTreeNode node, List<Path> digest) {
		digest.add(node.getFullPath());

		for (FileTreeNode child : node.getChildren()) {
			walkTrough(child, digest);
		}
	}

	@Override
	public void rollBack() {
		// nothing to undo
		getProcess().nextRollBackStep();
	}

}
