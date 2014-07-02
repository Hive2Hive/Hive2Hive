package org.hive2hive.core.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hive2hive.core.model.FileIndex;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.UserProfile;
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
public class FileSynchronizer {

	private static final Logger logger = LoggerFactory.getLogger(FileSynchronizer.class);

	private final Path root;
	private final UserProfile userProfile;

	private final FolderIndex profileRootNode;

	// Map<file-path, file-hash>
	private final Map<String, byte[]> before;
	private Map<String, byte[]> now;

	public FileSynchronizer(Path rootDirectory, UserProfile userProfile) throws IOException {
		this.root = rootDirectory;
		this.userProfile = userProfile;
		this.profileRootNode = userProfile.getRoot();

		// load the two file trees
		before = FileUtil.readPersistentMetaData(root).getFileTree();

		PersistenceFileVisitor visitor = new PersistenceFileVisitor(root);
		try {
			Files.walkFileTree(root, visitor);
			now = visitor.getFileTree();
		} catch (IOException e) {
			logger.error("Cannot walk the current tree.", e);
			now = new HashMap<String, byte[]>(0);
		}
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
				Index node = userProfile.getFileByPath(Paths.get(path));
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
	public List<Path> getDeletedRemotely() {
		List<Path> deletedRemotely = new ArrayList<Path>();

		for (String p : now.keySet()) {
			Path path = Paths.get(p);
			if (before.containsKey(p) && userProfile.getFileByPath(path) == null) {
				// is on disk but deleted in the user profile
				if (HashUtil.compare(before.get(p), now.get(p))) {
					// only delete the file, if it was not modified locally
					deletedRemotely.add(Paths.get(root.toString(), path.toString()));
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
	public List<Path> getAddedLocally() {
		List<Path> addedLocally = new ArrayList<Path>();

		for (String p : now.keySet()) {
			Path path = Paths.get(p);
			// test whether it is in the user profile
			Index node = userProfile.getFileByPath(path);
			if (node == null) {
				// not in profile --> it has been added locally
				logger.debug("File '{}' has been added locally during absence.", p);
				addedLocally.add(Paths.get(root.toString(), path.toString()));
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
			if (now.containsKey(index.getFullPath().toString())) {
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
	public List<Path> getUpdatedLocally() {
		List<Path> updatedLocally = new ArrayList<Path>();

		for (String path : now.keySet()) {
			if (!before.containsKey(path)) {
				// was not here before --> skip
				continue;
			}

			if (HashUtil.compare(before.get(path), now.get(path))) {
				// md5 before and after match --> nothing changed
				continue;
			}

			Index index = userProfile.getFileByPath(Paths.get(path));
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
				updatedLocally.add(FileUtil.getPath(root, fileNode));
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
			String path = fileIndex.getFullPath().toString();
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
				return node1.getFullPath().toString().compareTo(node2.getFullPath().toString());
			}
		});
	}

	/**
	 * Sorts a list of files in pre-order style
	 * 
	 * @param deletedLocally
	 */
	private void sortFilesPreorder(List<Path> fileList) {
		Collections.sort(fileList, new Comparator<Path>() {

			@Override
			public int compare(Path path1, Path path2) {
				return path1.compareTo(path2);
			}
		});
	}
}
