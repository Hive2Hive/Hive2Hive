package org.hive2hive.core.extras;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hive2hive.core.file.FileUtil;
import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.security.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to synchronize when a client comes online. It compares the meta data from last logout with the
 * current situation on disc and in the user profile.
 * 
 * @author Nico
 * 
 */
@Extra
public class FileSynchronizer {

	private static final Logger logger = LoggerFactory.getLogger(FileSynchronizer.class);

	private final File root;
	private final UserProfile userProfile;
	private final FolderIndex profileRootNode;

	// Map<file-path, file-hash>
	private final Map<String, byte[]> before;
	private final Map<String, byte[]> now;

	/**
	 * @param rootDirectory the root Hive2Hive directory
	 * @param userProfile the current user profile
	 * @param before represents the file state at the last logout, before H2H was shutdown. The key of the map
	 *            is the path, the byte[] is the hash of the file content.
	 *            {@link FileSynchronizer#visitFiles(File)} can be used to generate this map.
	 * @param now represents the current file state. The key of the map is the path, the byte[] is the hash of
	 *            the file content. {@link FileSynchronizer#visitFiles(File)} can be used to generate this
	 *            map.
	 */
	public FileSynchronizer(File rootDirectory, UserProfile userProfile, Map<String, byte[]> before, Map<String, byte[]> now) {
		this.root = rootDirectory;
		this.userProfile = userProfile;
		this.before = before;
		this.now = now;
		this.profileRootNode = userProfile.getRoot();
	}

	/**
	 * Returns a list of files that have been deleted from the disc during this client was offline
	 * 
	 * @return
	 */
	public List<Index> getDeletedLocally() {
		List<Index> deletedLocally = new ArrayList<Index>();

		for (String path : before.keySet()) {
			if (now.containsKey(path)) {
				// skip, this file is still here
				continue;
			} else {
				// test whether it is in the user profile
				Index node = userProfile.getFileByPath(new File(root, path), root);
				if (node != null) {
					// file is still in user profile
					if (node.isFolder()) {
						deletedLocally.add(node);
					} else {
						// check the MD5 value to not delete a modified file
						FileIndex fileNode = (FileIndex) node;
						if (HashUtil.compare(fileNode.getMD5(), before.get(path))) {
							// file has not been modified remotely, delete it
							logger.debug("File '{}' has been deleted locally during absence.", path);
							deletedLocally.add(node);
						}
					}
				}
			}
		}

		// delete from behind
		sortNodesPreorder(deletedLocally);
		Collections.reverseOrder();

		logger.info("Found {} files/folders that have been deleted locally during absence.", deletedLocally.size());
		return deletedLocally;
	}

	/**
	 * Returns a list of files that have been deleted by another client during the absence of this client.
	 * 
	 * @return
	 */
	public List<File> getDeletedRemotely() {
		List<File> deletedRemotely = new ArrayList<File>();

		for (String p : now.keySet()) {
			File file = new File(root, p);
			if (before.containsKey(p) && userProfile.getFileByPath(file, root) == null) {
				// is on disk but deleted in the user profile
				if (HashUtil.compare(before.get(p), now.get(p))) {
					// only delete the file, if it was not modified locally
					deletedRemotely.add(file);
				}
			}
		}

		logger.debug("Found {} files/folders that have been deleted remotely during absence.", deletedRemotely.size());
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

		for (String p : now.keySet()) {
			File file = new File(root, p);
			// test whether it is in the user profile
			Index node = userProfile.getFileByPath(file, root);
			if (node == null) {
				// not in profile --> it has been added locally
				logger.debug("File '{}' has been added locally during absence.", p);
				addedLocally.add(file);
			}
		}

		sortFilesPreorder(addedLocally);
		logger.info("Found {} files/folders that have been added locally during absence.", addedLocally.size());
		return addedLocally;
	}

	/**
	 * Returns a list of files that are in the user profile but not on the local disk yet.
	 * 
	 * @return
	 */
	public List<Index> getAddedRemotely() {
		List<Index> addedRemotely = new ArrayList<Index>();

		// visit all files in the tree and compare to disk
		List<Index> indexList = Index.getIndexList(profileRootNode);
		indexList.remove(profileRootNode);

		for (Index index : indexList) {
			if (now.containsKey(index.getFullPath())) {
				// was here before and is still here --> nothing to add
				logger.trace("File '{}' was already here.", index.getFullPath());
			} else {
				logger.debug("File '{}' has been added remotely during absence.", index.getFullPath());
				addedRemotely.add(index);
			}
		}

		sortNodesPreorder(addedRemotely);
		logger.info("Found {} files/folders that have been added remotely during absence.", addedRemotely.size());
		return addedRemotely;
	}

	/**
	 * Returns a list of files that already existed but have been modified by the client while he was offline.
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

			if (HashUtil.compare(before.get(path), now.get(path))) {
				// md5 before and after match --> nothing changed
				continue;
			}

			File file = new File(root, path);
			Index index = userProfile.getFileByPath(file, root);
			if (index == null || index.isFolder()) {
				// file not found --> skip, this is not the task of this method
				// file node is a folder --> cannot compare the modification
				continue;
			}

			FileIndex fileNode = (FileIndex) index;

			// has been modified --> check if profile has same md5 as 'before'. If not, there are three
			// different versions. Thus, the profile wins.
			if (HashUtil.compare(fileNode.getMD5(), before.get(path)) && !HashUtil.compare(fileNode.getMD5(), now.get(path))) {
				logger.debug("File '{}' has been updated locally during absence.", path);
				updatedLocally.add(file);
			}
		}

		sortFilesPreorder(updatedLocally);
		logger.info("Found {} files/folders that have been updated locally during absence.", updatedLocally.size());
		return updatedLocally;
	}

	/**
	 * Returns files that have been remotely modified while the client was offline
	 * 
	 * @return
	 */
	public List<FileIndex> getUpdatedRemotely() {
		List<FileIndex> updatedRemotely = new ArrayList<FileIndex>();

		// visit all files in the tree and compare to disk
		List<Index> indexList = Index.getIndexList(profileRootNode);
		for (Index index : indexList) {
			if (index.isFolder()) {
				// folder cannot be modified
				continue;
			}

			FileIndex fileIndex = (FileIndex) index;
			String path = fileIndex.getFullPath();
			if (before.containsKey(path) && now.containsKey(path)) {
				if (!HashUtil.compare(fileIndex.getMD5(), now.get(path))
						&& !HashUtil.compare(fileIndex.getMD5(), before.get(path))) {
					// different md5 hashes than 'before' and 'now'
					logger.debug("File '{}' has been updated remotely during absence.", path);
					updatedRemotely.add(fileIndex);
				}
			}
		}

		logger.info("Found {} files/folders that have been updated remotely during absence.", updatedRemotely.size());
		return updatedRemotely;
	}

	/**
	 * Sorts a list of {@link FolderIndex} in pre-order style
	 * 
	 * @param deletedLocally
	 */
	private void sortNodesPreorder(List<Index> fileList) {
		Collections.sort(fileList, new Comparator<Index>() {

			@Override
			public int compare(Index node1, Index node2) {
				return node1.getFullPath().compareTo(node2.getFullPath());
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
				return file1.compareTo(file2);
			}
		});
	}

	/**
	 * Visit all files recursively and calculate the hash of the file. Folders are also added to the result.
	 * 
	 * @param root the root folder
	 * @return a map where the key is the relative file path to the root and the value is the hash
	 * @throws IOException if hashing fails
	 */
	public static Map<String, byte[]> visitFiles(File root) throws IOException {
		Map<String, byte[]> digest = new HashMap<String, byte[]>();
		Iterator<File> files = FileUtils.iterateFilesAndDirs(root, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
		while (files.hasNext()) {
			File file = files.next();
			if (file.equals(root)) {
				// skip root folder
				continue;
			}
			String path = FileUtil.relativize(root, file).toString();
			byte[] hash = HashUtil.hash(file);
			if (file.isDirectory()) {
				digest.put(path + FileUtil.getFileSep(), hash);
			} else {
				digest.put(path, hash);
			}
		}
		return digest;
	}
}
