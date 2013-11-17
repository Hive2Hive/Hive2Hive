package org.hive2hive.core.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.UserProfile;
import org.hive2hive.core.security.H2HEncryptionUtil;

/**
 * Helps to synchronize when a client comes online. It compares the meta data from last logout with the
 * current situation on disc and in the user profile.
 * 
 * @author Nico
 * 
 */
public class FileSynchronizer {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(FileSynchronizer.class);

	private final FileManager fileManager;
	private final UserProfile userProfile;

	private final FileTreeNode profileRootNode;
	private final Map<String, byte[]> before;
	private Map<String, byte[]> now;

	public FileSynchronizer(FileManager fileManager, UserProfile userProfile) {
		this.fileManager = fileManager;
		this.userProfile = userProfile;
		this.profileRootNode = userProfile.getRoot();

		// load the two file trees
		before = fileManager.getPersistentMetaData().getFileTree();

		PersistenceFileVisitor visitor = new PersistenceFileVisitor(fileManager.getRoot());
		try {
			Files.walkFileTree(fileManager.getRoot().toPath(), visitor);
			now = visitor.getFileTree();
		} catch (IOException e) {
			logger.error("Cannot walk the current tree");
			now = new HashMap<String, byte[]>(0);
		}
	}

	/**
	 * Returns a list of files that have been deleted from the disc during this client was offline
	 * 
	 * @return
	 */
	public List<FileTreeNode> getDeletedLocally() {
		List<FileTreeNode> deletedLocally = new ArrayList<FileTreeNode>();

		for (String path : before.keySet()) {
			if (now.containsKey(path)) {
				// skip, this file is still here
				continue;
			} else {
				// test whether it is in the user profile
				FileTreeNode node = userProfile.getFileByPath(path);
				if (node != null) {
					logger.info("File " + path + " has been deleted locally during absence");
					deletedLocally.add(node);
				}
			}
		}

		// TODO order in pre-order (by path name)
		logger.debug("Found " + deletedLocally.size()
				+ " files/folders that have been deleted locally during absence");
		return deletedLocally;
	}

	/**
	 * Returns a list of files that have been deleted by another client during the absence of this client.
	 * 
	 * @return
	 */
	public List<File> getDeletedRemotely() {
		List<File> deletedRemotely = new ArrayList<File>();

		for (String path : now.keySet()) {
			if (before.containsKey(path) && userProfile.getFileByPath(path) == null) {
				// is on disk but deleted in the user profile
				deletedRemotely.add(new File(fileManager.getRoot(), path));
			}
		}

		logger.debug("Found " + deletedRemotely.size()
				+ " files/folders that have been deleted remotely during absence");
		return deletedRemotely;
	}

	/**
	 * Returns the missing files that exist on disk but not in the file tree in the user profile. The list is
	 * in pre-order
	 * 
	 * @return
	 */
	public List<File> getAddedLocally() {
		List<File> addedLocally = new ArrayList<File>();

		for (String path : now.keySet()) {
			if (before.containsKey(path)) {
				// skip, was here before
				continue;
			} else {
				try {
					// test whether it is in the user profile
					FileTreeNode node = userProfile.getFileByPath(path);
					if (node == null) {
						throw new FileNotFoundException();
					}
				} catch (FileNotFoundException e) {
					// not in profile --> it has been added locally
					logger.info("File " + path + " has been added locally during absence");
					addedLocally.add(new File(fileManager.getRoot(), path));
				}
			}
		}

		// TODO order in pre-order (by path name)
		logger.debug("Found " + addedLocally.size()
				+ " files/folders that have been added locally during absence");
		return addedLocally;
	}

	/**
	 * Returns a list of files that are in the user profile but not on the local disk yet.
	 * 
	 * @return
	 */
	public List<FileTreeNode> getAddedRemotely() {
		List<FileTreeNode> addedRemotely = new ArrayList<FileTreeNode>();

		// visit all files in the tree and compare to disk
		Stack<FileTreeNode> fileStack = new Stack<FileTreeNode>();
		fileStack.addAll(profileRootNode.getChildren());
		while (!fileStack.isEmpty()) {
			FileTreeNode top = fileStack.pop();
			if (before.containsKey(top.getFullPath()) && now.containsKey(top.getFullPath())) {
				// was here before and is still here --> nothing to add
				logger.trace("File " + top.getFullPath() + " was already here");
			} else {
				logger.info("File " + top.getFullPath() + " has been added remotely during absence");
				addedRemotely.add(top);
			}

			// add children to stack
			for (FileTreeNode child : top.getChildren()) {
				fileStack.push(child);
			}
		}

		logger.debug("Found " + addedRemotely.size()
				+ " files/folders that have been added remotely during absence");
		return addedRemotely;
	}

	/**
	 * Returns a list of files that already exist but have been modified by the client while he was offline.
	 * 
	 * @return
	 */
	public List<File> getUpdatedLocally() {
		List<File> updatedLocally = new ArrayList<File>();

		for (String path : now.keySet()) {
			if (!before.containsKey(path)) {
				// was not here before --> skip
				continue;
			}

			if (H2HEncryptionUtil.compareMD5(before.get(path), now.get(path))) {
				// md5 before and after match --> nothing changed
				continue;
			}

			FileTreeNode fileNode = userProfile.getFileByPath(path);
			if (fileNode == null) {
				// file not found --> skip, this is not the task of this method
				continue;
			}

			// has been modified --> check if profile has same md5 as 'before'. If not, there are three
			// different versions. Thus, the profile wins.
			// TODO handle conflicts if three different versions
			if (H2HEncryptionUtil.compareMD5(fileNode.getMD5(), before.get(path))
					&& !H2HEncryptionUtil.compareMD5(fileNode.getMD5(), now.get(path))) {
				logger.info("File " + path + " has been updated locally during absence");
				updatedLocally.add(fileManager.getFile(fileNode));
			}
		}

		// TODO order in pre-order (by path name)
		logger.debug("Found " + updatedLocally.size()
				+ " files/folders that have been updated locally during absence");
		return updatedLocally;
	}

	/**
	 * Returns files that have been remotely modified while the client was offline
	 * 
	 * @return
	 */
	public List<FileTreeNode> getUpdatedRemotely() {
		List<FileTreeNode> updatedRemotely = new ArrayList<FileTreeNode>();

		// visit all files in the tree and compare to disk
		Stack<FileTreeNode> fileStack = new Stack<FileTreeNode>();
		fileStack.addAll(profileRootNode.getChildren());
		while (!fileStack.isEmpty()) {
			FileTreeNode top = fileStack.pop();
			if (before.containsKey(top.getFullPath()) && now.containsKey(top.getFullPath())) {
				// was here before and is still here
				if (!H2HEncryptionUtil.compareMD5(top.getMD5(), now.get(top.getFullPath()))
						&& !H2HEncryptionUtil.compareMD5(top.getMD5(), before.get(top.getFullPath()))) {
					// different md5 hashes than 'before' and 'now'
					logger.info("File " + top.getFullPath() + " has been updated remotely during absence");
					updatedRemotely.add(top);
				}
			}

			// add children to stack
			for (FileTreeNode child : top.getChildren()) {
				fileStack.push(child);
			}
		}

		logger.debug("Found " + updatedRemotely.size()
				+ " files/folders that have been updated remotely during absence");
		return updatedRemotely;
	}
}
