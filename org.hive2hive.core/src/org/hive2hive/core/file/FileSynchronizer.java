package org.hive2hive.core.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
					// file is still in user profile
					if (H2HEncryptionUtil.compareMD5(node.getMD5(), before.get(path))) {
						// file has not been modified remotely, delete it
						logger.debug("File " + path + " has been deleted locally during absence");
						deletedLocally.add(node);
					}
				}
			}
		}

		// delete from behind
		sortNodesPreorder(deletedLocally);
		Collections.reverseOrder();

		logger.info("Found " + deletedLocally.size()
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
				if (H2HEncryptionUtil.compareMD5(before.get(path), now.get(path))) {
					// only delete the file, if it was not modified locally
					deletedRemotely.add(new File(fileManager.getRoot(), path));
				}
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
			// test whether it is in the user profile
			FileTreeNode node = userProfile.getFileByPath(path);
			if (node == null) {
				// not in profile --> it has been added locally
				logger.debug("File " + path + " has been added locally during absence");
				addedLocally.add(new File(fileManager.getRoot(), path));
			}
		}

		sortFilesPreorder(addedLocally);
		logger.info("Found " + addedLocally.size()
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
			if (now.containsKey(top.getFullPath())) {
				// was here before and is still here --> nothing to add
				logger.trace("File " + top.getFullPath() + " was already here");
			} else {
				logger.debug("File " + top.getFullPath() + " has been added remotely during absence");
				addedRemotely.add(top);
			}

			// add children to stack
			for (FileTreeNode child : top.getChildren()) {
				fileStack.push(child);
			}
		}

		sortNodesPreorder(addedRemotely);
		logger.info("Found " + addedRemotely.size()
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
			if (H2HEncryptionUtil.compareMD5(fileNode.getMD5(), before.get(path))
					&& !H2HEncryptionUtil.compareMD5(fileNode.getMD5(), now.get(path))) {
				logger.debug("File " + path + " has been updated locally during absence");
				updatedLocally.add(fileManager.getFile(fileNode));
			}
		}

		sortFilesPreorder(updatedLocally);
		logger.info("Found " + updatedLocally.size()
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
					logger.debug("File " + top.getFullPath() + " has been updated remotely during absence");
					updatedRemotely.add(top);
				}
			}

			// add children to stack
			for (FileTreeNode child : top.getChildren()) {
				fileStack.push(child);
			}
		}

		logger.info("Found " + updatedRemotely.size()
				+ " files/folders that have been updated remotely during absence");
		return updatedRemotely;
	}

	/**
	 * Sorts a list of {@link FileTreeNode} in pre-order style
	 * 
	 * @param deletedLocally
	 */
	private void sortNodesPreorder(List<FileTreeNode> fileList) {
		Collections.sort(fileList, new Comparator<FileTreeNode>() {

			@Override
			public int compare(FileTreeNode node1, FileTreeNode node2) {
				return new String(node1.getFullPath()).compareTo(node2.getFullPath());
			}
		});
	}

	/**
	 * Sorts a list of files in pre-order style
	 * 
	 * @param deletedLocally
	 */
	private void sortFilesPreorder(List<File> fileList) {
		Collections.sort(fileList, new Comparator<File>() {

			@Override
			public int compare(File file1, File file2) {
				return new String(file1.getAbsolutePath()).compareTo(file2.getAbsolutePath());
			}
		});
	}
}
