package org.hive2hive.core.processes.share.write;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.exceptions.GetFailedException;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.FolderIndex;
import org.hive2hive.core.model.Index;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.model.versioned.UserProfile;
import org.hive2hive.core.processes.share.BaseShareReadWriteTest;
import org.hive2hive.core.utils.FileTestUtil;
import org.hive2hive.core.utils.UseCaseTestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A folder is shared with {@link PermissionType#WRITE} permission. Tests moving files and folder within a
 * shared folder.
 * 
 * @author Seppi
 * @author Nico
 */
public class SharedFolderWithWritePermissionMoveInternalTest extends BaseShareReadWriteTest {

	private File subFolder1AtA;
	private File subFolder1AtB;
	private File subFolder2AtA;
	private File subFolder2AtB;

	@BeforeClass
	public static void printIdentifier() throws Exception {
		testClass = SharedFolderWithWritePermissionMoveInternalTest.class;
		beforeClass();
		setupNetwork();
	}

	@Before
	public void initTest() throws Exception {
		setupShares(PermissionType.WRITE);

		logger.info("Upload a new subfolder 'sharedfolder/subfolder1'.");
		subFolder1AtA = new File(sharedFolderA, "subfolder1");
		subFolder1AtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder1AtA);
		subFolder1AtB = new File(sharedFolderB, subFolder1AtA.getName());
		waitTillSynchronized(subFolder1AtB, true);

		logger.info("Upload a new subfolder 'sharedfolder/subfolder2'.");
		subFolder2AtA = new File(sharedFolderA, "subfolder2");
		subFolder2AtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), subFolder2AtA);
		subFolder2AtB = new File(sharedFolderB, subFolder2AtA.getName());
		waitTillSynchronized(subFolder2AtB, true);
	}

	@Test
	public void testSynchronizeAddFileFromAMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/file1FromA' from A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file 'sharedFolder/file1FromA' gets synchronized with B.");
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file 'fileFromA' at A into shared subfolder 'sharedfolder/subfolder1'.");
		File movedFileFromAAtA = new File(subFolder1AtA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file 'file1FromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(subFolder1AtB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFileFromAMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/file2FromA' from A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("file2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderA);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file 'sharedFolder/file2FromA' gets synchronized with B.");
		File fileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file 'file2FromA' at B into shared subfolder 'sharedfolder/subfolder1'.");
		File movedFileFromAAtB = new File(subFolder1AtB, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtB, movedFileFromAAtB);
		UseCaseTestUtil.moveFile(network.get(1), fileFromAAtB, movedFileFromAAtB);

		logger.info("Wait till new moved file 'file2FromA' gets synchronized with A.");
		File movedFileFromAAtA = new File(subFolder1AtA, fileFromAAtB.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFileFromBMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/file1FromB' from B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("file1FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderB);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file 'sharedFolder/file1FromB' gets synchronized with A.");
		File fileFromBAtA = new File(sharedFolderA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Move file 'file1FromB' at A into shared subfolder 'sharedfolder/subfolder1'.");
		File movedFileFromAAtA = new File(subFolder1AtA, fileFromBAtA.getName());
		FileUtils.moveFile(fileFromBAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), fileFromBAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file 'file1FromB' gets synchronized with B.");
		File movedFileFromAAtB = new File(subFolder1AtB, fileFromBAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromBAtA, fileFromBAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddFileFromBMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/file2FromB' from B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("file2FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				sharedFolderB);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file 'sharedFolder/file2FromB' gets synchronized with A.");
		File fileFromBAtA = new File(sharedFolderB, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Move file 'file2FromB' at B into shared subfolder 'sharedfolder/subfolder1'.");
		File movedFileFromBAtB = new File(subFolder1AtB, fileFromBAtB.getName());
		FileUtils.moveFile(fileFromBAtB, movedFileFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved file 'fileFromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(subFolder1AtA, fileFromBAtA.getName());
		waitTillSynchronized(movedFileFromBAtA, true);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(fileFromBAtA, fileFromBAtB, movedFileFromBAtA, movedFileFromBAtB);
	}

	@Test
	public void testSynchronizeAddFolderFromAMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1FromA' from A.");
		File folderFromAAtA = new File(sharedFolderA, "subfolder1FromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Wait till new folder 'sharedFolder/subfolder1FromA' gets synchronized with B.");
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder 'subfolder1FromA' at A into shared subfolder 'sharedfolder/subfolder1'.");
		File movedFolderFromAAtA = new File(subFolder1AtA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till moved folder 'subfolder1FromA' gets synchronized with B.");
		File movedFolderFromAAtB = new File(subFolder1AtB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddFolderFromAMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder2FromA' from A.");
		File folderFromAAtA = new File(sharedFolderA, "subfolder2FromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Wait till new folder 'sharedFolder/subfolder2FromA' gets synchronized with B.");
		File folderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder 'subfolder2FromA' at B into shared subfolder 'sharedfolder/subfolder1'.");
		File movedFolderFromAAtB = new File(subFolder1AtB, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtB, movedFolderFromAAtB);
		UseCaseTestUtil.moveFile(network.get(1), folderFromAAtB, movedFolderFromAAtB);

		logger.info("Wait till moved folder 'subfolder2FromA' gets synchronized with A.");
		File movedFileFromAAtA = new File(subFolder1AtA, folderFromAAtB.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, movedFileFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddFolderFromBMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1FromB' from B.");
		File folderFromBAtB = new File(sharedFolderB, "subfolder1FromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Wait till new folder 'sharedFolder/subfolder1FromB' gets synchronized with A.");
		File folderFromBAtA = new File(sharedFolderA, folderFromBAtB.getName());
		waitTillSynchronized(folderFromBAtA, true);

		logger.info("Move folder 'subfolder1FromB' at A into shared subfolder 'sharedfolder/subfolder1'.");
		File movedFolderFromAAtA = new File(subFolder1AtA, folderFromBAtA.getName());
		FileUtils.moveDirectory(folderFromBAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromBAtA, movedFolderFromAAtA);

		logger.info("Wait till moved folder 'subfolder1FromB' gets synchronized with B.");
		File movedFolderFromAAtB = new File(subFolder1AtB, folderFromBAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromBAtA, folderFromBAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddFolderFromBMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder2FromB' from B.");
		File folderFromBAtB = new File(sharedFolderB, "subfolder2FromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Wait till new folder 'sharedFolder/subfolder2FromB' gets synchronized with A.");
		File folderFromBAtA = new File(sharedFolderB, folderFromBAtB.getName());
		waitTillSynchronized(folderFromBAtA, true);

		logger.info("Move folder 'subfolder2FromB' at B into shared subfolder 'sharedfolder/subfolder1'.");
		File movedFolderFromBAtB = new File(subFolder1AtB, folderFromBAtB.getName());
		FileUtils.moveDirectory(folderFromBAtB, movedFolderFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), folderFromBAtB, movedFolderFromBAtB);

		logger.info("Wait till moved folder 'subfolderFromB' gets synchronized with A.");
		File movedFolderFromBAtA = new File(subFolder1AtA, folderFromBAtA.getName());
		waitTillSynchronized(movedFolderFromBAtA, true);
		compareFiles(movedFolderFromBAtA, movedFolderFromBAtB);
		checkIndex(folderFromBAtA, folderFromBAtB, movedFolderFromBAtA, movedFolderFromBAtB);
	}

	@Test
	public void testSynchronizeAddSubfileFromAMoveToFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/subfolder1/subfile1FromA' from A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile1FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1AtA);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file 'sharedFolder/subfolder1/subfile1FromA' gets synchronized with B.");
		File fileFromAAtB = new File(subFolder1AtB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file 'subfileFromA' at A into shared folder 'sharedfolder'.");
		File movedFileFromAAtA = new File(sharedFolderA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file 'subfile1FromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileFromAMoveToFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/subfolder1/subfile2FromA' from A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile2FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1AtA);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file 'sharedFolder/subfolder1/subfile2FromA' gets synchronized with B.");
		File fileFromAAtB = new File(subFolder1AtB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file 'subfile2FromA' at B into shared subfolder 'sharedfolder'.");
		File movedFileFromAAtB = new File(sharedFolderB, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtB, movedFileFromAAtB);
		UseCaseTestUtil.moveFile(network.get(1), fileFromAAtB, movedFileFromAAtB);

		logger.info("Wait till new moved file 'subfile2FromA' gets synchronized with A.");
		File movedFileFromAAtA = new File(sharedFolderA, fileFromAAtB.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileFromBMoveToFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/subfolder1/subfile1FromB' from B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfile1FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1AtB);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file 'sharedFolder/subfolder1/subfile1FromB' gets synchronized with A.");
		File fileFromBAtA = new File(subFolder1AtA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Move file 'subfile1FromB' at A into shared subfolder 'sharedfolder'.");
		File movedFileFromAAtA = new File(sharedFolderA, fileFromBAtA.getName());
		FileUtils.moveFile(fileFromBAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), fileFromBAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file 'subfile1FromB' gets synchronized with B.");
		File movedFileFromAAtB = new File(sharedFolderB, fileFromBAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromBAtA, fileFromBAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileFromBMoveToFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/subfolder1/subfile2FromB' from B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfile2FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1AtB);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file 'sharedFolder/subfolder1/subfile2FromB' gets synchronized with A.");
		File fileFromBAtA = new File(subFolder1AtA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Move file 'subfile2FromB' at B into shared subfolder 'sharedfolder'.");
		File movedFileFromBAtB = new File(sharedFolderB, fileFromBAtB.getName());
		FileUtils.moveFile(fileFromBAtB, movedFileFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved file 'subfileFromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(sharedFolderA, fileFromBAtA.getName());
		waitTillSynchronized(movedFileFromBAtA, true);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(fileFromBAtA, fileFromBAtB, movedFileFromBAtA, movedFileFromBAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromAMoveToFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1/subsubfolder1FromA' from A.");
		File folderFromAAtA = new File(subFolder1AtA, "subsubfolder1FromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Wait till new folder 'sharedFolder/subfolder1/subsubfolder1FromA' gets synchronized with B.");
		File folderFromAAtB = new File(subFolder1AtB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder 'subsubfolder1FromA' at A into shared subfolder 'sharedfolder'.");
		File movedFolderFromAAtA = new File(sharedFolderA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till moved folder 'subsubfolder1FromA' gets synchronized with B.");
		File movedFolderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromAMoveToFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1/subsubfolder2FromA' from A.");
		File folderFromAAtA = new File(subFolder1AtA, "subsubfolder2FromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Wait till new folder 'sharedFolder/subfolder1/subsubfolder2FromA' gets synchronized with B.");
		File folderFromAAtB = new File(subFolder1AtB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder 'subsubfolder2FromA' at B into shared subfolder 'sharedfolder'.");
		File movedFolderFromAAtB = new File(sharedFolderB, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtB, movedFolderFromAAtB);
		UseCaseTestUtil.moveFile(network.get(1), folderFromAAtB, movedFolderFromAAtB);

		logger.info("Wait till moved folder 'subsubfolder2FromA' gets synchronized with A.");
		File movedFileFromAAtA = new File(sharedFolderA, folderFromAAtB.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, movedFileFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromBMoveToFolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1/subsubfolder1FromB' from B.");
		File folderFromBAtB = new File(subFolder1AtB, "subsubfolder1FromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Wait till new folder 'sharedFolder/subfolder1/subsubfolder1FromB' gets synchronized with A.");
		File folderFromBAtA = new File(subFolder1AtA, folderFromBAtB.getName());
		waitTillSynchronized(folderFromBAtA, true);

		logger.info("Move folder 'subsubfolder1FromB' at A into shared subfolder 'sharedfolder'.");
		File movedFolderFromAAtA = new File(sharedFolderA, folderFromBAtA.getName());
		FileUtils.moveDirectory(folderFromBAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromBAtA, movedFolderFromAAtA);

		logger.info("Wait till moved folder 'subsubfolder1FromB' gets synchronized with B.");
		File movedFolderFromAAtB = new File(sharedFolderB, folderFromBAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromBAtA, folderFromBAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromBMoveToFolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1/subsubfolder2FromB' from B.");
		File folderFromBAtB = new File(subFolder1AtB, "subsubfolder2FromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Wait till new folder 'sharedFolder/subfolder1/subsubfolder2FromB' gets synchronized with A.");
		File folderFromBAtA = new File(subFolder1AtA, folderFromBAtB.getName());
		waitTillSynchronized(folderFromBAtA, true);

		logger.info("Move folder 'subsubfolder2FromB' at B into shared subfolder 'sharedfolder'.");
		File movedFolderFromBAtB = new File(sharedFolderB, folderFromBAtB.getName());
		FileUtils.moveDirectory(folderFromBAtB, movedFolderFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), folderFromBAtB, movedFolderFromBAtB);

		logger.info("Wait till moved folder 'subsubfolderFromB' gets synchronized with A.");
		File movedFolderFromBAtA = new File(sharedFolderA, folderFromBAtA.getName());
		waitTillSynchronized(movedFolderFromBAtA, true);
		compareFiles(movedFolderFromBAtA, movedFolderFromBAtB);
		checkIndex(folderFromBAtA, folderFromBAtB, movedFolderFromBAtA, movedFolderFromBAtB);
	}

	@Test
	public void testSynchronizeAddSubfileFromAMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/subfolder1/subfile3FromA' from A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile3FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1AtA);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file 'sharedFolder/subfolder1/subfile3FromA' gets synchronized with B.");
		File fileFromAAtB = new File(subFolder1AtB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file 'subfile3FromA' at A into shared subfolder 'sharedfolder/subfolder2'.");
		File movedFileFromAAtA = new File(subFolder2AtA, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), fileFromAAtA, movedFileFromAAtA);

		logger.info("Wait till moved file 'subfile3FromA' gets synchronized with B.");
		File movedFileFromAAtB = new File(subFolder2AtB, fileFromAAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileFromAMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/subfolder1/subfile4FromA' from A.");
		File fileFromAAtA = FileTestUtil.createFileRandomContent("subfile4FromA", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1AtA);
		UseCaseTestUtil.uploadNewFile(network.get(0), fileFromAAtA);

		logger.info("Wait till new file 'sharedFolder/subfolder1/subfile4FromA' gets synchronized with B.");
		File fileFromAAtB = new File(subFolder1AtB, fileFromAAtA.getName());
		waitTillSynchronized(fileFromAAtB, true);

		logger.info("Move file 'subfile4FromA' at B into shared subfolder 'sharedfolder/subfolder2'.");
		File movedFileFromAAtB = new File(subFolder2AtB, fileFromAAtA.getName());
		FileUtils.moveFile(fileFromAAtB, movedFileFromAAtB);
		UseCaseTestUtil.moveFile(network.get(1), fileFromAAtB, movedFileFromAAtB);

		logger.info("Wait till new moved file 'subfile4FromA' gets synchronized with A.");
		File movedFileFromAAtA = new File(subFolder2AtA, fileFromAAtB.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromAAtA, fileFromAAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileFromBMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/subfolder1/subfile3FromB' from B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfile3FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1AtB);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file 'sharedFolder/subfolder1/subfile3FromB' gets synchronized with A.");
		File fileFromBAtA = new File(subFolder1AtA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Move file 'subfile3FromB' at A into shared subfolder 'sharedfolder/subfolder2'.");
		File movedFileFromAAtA = new File(subFolder2AtA, fileFromBAtA.getName());
		FileUtils.moveFile(fileFromBAtA, movedFileFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), fileFromBAtA, movedFileFromAAtA);

		logger.info("Wait till new moved file 'subfile3FromB' gets synchronized with B.");
		File movedFileFromAAtB = new File(subFolder2AtB, fileFromBAtA.getName());
		waitTillSynchronized(movedFileFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFileFromAAtB);
		checkIndex(fileFromBAtA, fileFromBAtB, movedFileFromAAtA, movedFileFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfileFromBMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new file 'sharedfolder/subfolder1/subfile4FromB' from B.");
		File fileFromBAtB = FileTestUtil.createFileRandomContent("subfile4FromB", new Random().nextInt(MAX_NUM_CHUNKS) + 1,
				subFolder1AtB);
		UseCaseTestUtil.uploadNewFile(network.get(1), fileFromBAtB);

		logger.info("Wait till new file 'sharedFolder/subfolder1/subfile4FromB' gets synchronized with A.");
		File fileFromBAtA = new File(subFolder1AtA, fileFromBAtB.getName());
		waitTillSynchronized(fileFromBAtA, true);

		logger.info("Move file 'subfile4FromB' at B into shared subfolder 'sharedfolder/subfolder2'.");
		File movedFileFromBAtB = new File(subFolder2AtB, fileFromBAtB.getName());
		FileUtils.moveFile(fileFromBAtB, movedFileFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), fileFromBAtB, movedFileFromBAtB);

		logger.info("Wait till new moved file 'subfile4FromB' gets synchronized with A.");
		File movedFileFromBAtA = new File(subFolder2AtA, fileFromBAtA.getName());
		waitTillSynchronized(movedFileFromBAtA, true);
		compareFiles(movedFileFromBAtA, movedFileFromBAtB);
		checkIndex(fileFromBAtA, fileFromBAtB, movedFileFromBAtA, movedFileFromBAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromAMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1/subsubfolder3FromA' from A.");
		File folderFromAAtA = new File(subFolder1AtA, "subsubfolder3FromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Wait till new folder 'sharedFolder/subfolder1/subsubfolder3FromA' gets synchronized with B.");
		File folderFromAAtB = new File(subFolder1AtB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder 'subsubfolder3FromA' at A into shared subfolder 'sharedfolder/subfolder2'.");
		File movedFolderFromAAtA = new File(subFolder2AtA, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromAAtA, movedFolderFromAAtA);

		logger.info("Wait till moved folder 'subsubfolder3FromA' gets synchronized with B.");
		File movedFolderFromAAtB = new File(subFolder2AtB, folderFromAAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromAMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1/subsubfolder4FromA' from A.");
		File folderFromAAtA = new File(subFolder1AtA, "subsubfolder4FromA");
		folderFromAAtA.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(0), folderFromAAtA);

		logger.info("Wait till new folder 'sharedFolder/subfolder1/subsubfolder4FromA' gets synchronized with B.");
		File folderFromAAtB = new File(subFolder1AtB, folderFromAAtA.getName());
		waitTillSynchronized(folderFromAAtB, true);

		logger.info("Move folder 'subsubfolder4FromA' at B into shared subfolder 'sharedfolder/subfolder2'.");
		File movedFolderFromAAtB = new File(subFolder2AtB, folderFromAAtA.getName());
		FileUtils.moveDirectory(folderFromAAtB, movedFolderFromAAtB);
		UseCaseTestUtil.moveFile(network.get(1), folderFromAAtB, movedFolderFromAAtB);

		logger.info("Wait till moved folder 'subsubfolder4FromA' gets synchronized with A.");
		File movedFileFromAAtA = new File(subFolder2AtA, folderFromAAtB.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFileFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromAAtA, folderFromAAtB, movedFileFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromBMoveToSubfolderAtA() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1/subsubfolder3FromB' from B.");
		File folderFromBAtB = new File(subFolder1AtB, "subsubfolder3FromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Wait till new folder 'sharedFolder/subfolder1/subsubfolder3FromB' gets synchronized with A.");
		File folderFromBAtA = new File(subFolder1AtA, folderFromBAtB.getName());
		waitTillSynchronized(folderFromBAtA, true);

		logger.info("Move folder 'subsubfolder3FromB' at A into shared subfolder 'sharedfolder/subfolder2'.");
		File movedFolderFromAAtA = new File(subFolder2AtA, folderFromBAtA.getName());
		FileUtils.moveDirectory(folderFromBAtA, movedFolderFromAAtA);
		UseCaseTestUtil.moveFile(network.get(0), folderFromBAtA, movedFolderFromAAtA);

		logger.info("Wait till moved folder 'subsubfolder3FromB' gets synchronized with B.");
		File movedFolderFromAAtB = new File(subFolder2AtB, folderFromBAtA.getName());
		waitTillSynchronized(movedFolderFromAAtB, true);
		compareFiles(movedFolderFromAAtA, movedFolderFromAAtB);
		checkIndex(folderFromBAtA, folderFromBAtB, movedFolderFromAAtA, movedFolderFromAAtB);
	}

	@Test
	public void testSynchronizeAddSubfolderFromBMoveToSubfolderAtB() throws NoSessionException, NoPeerConnectionException,
			IOException, IllegalArgumentException, IllegalArgumentException, GetFailedException {
		logger.info("Upload a new folder 'sharedfolder/subfolder1/subsubfolder4FromB' from B.");
		File folderFromBAtB = new File(subFolder1AtB, "subsubfolder4FromB");
		folderFromBAtB.mkdir();
		UseCaseTestUtil.uploadNewFile(network.get(1), folderFromBAtB);

		logger.info("Wait till new folder 'sharedFolder/subfolder1/subsubfolder4FromB' gets synchronized with A.");
		File folderFromBAtA = new File(subFolder1AtA, folderFromBAtB.getName());
		waitTillSynchronized(folderFromBAtA, true);

		logger.info("Move folder 'subsubfolder4FromB' at B into shared subfolder 'sharedfolder/subfolder2'.");
		File movedFolderFromBAtB = new File(subFolder2AtB, folderFromBAtB.getName());
		FileUtils.moveDirectory(folderFromBAtB, movedFolderFromBAtB);
		UseCaseTestUtil.moveFile(network.get(1), folderFromBAtB, movedFolderFromBAtB);

		logger.info("Wait till moved folder 'subsubfolder4FromB' gets synchronized with A.");
		File movedFolderFromBAtA = new File(subFolder2AtA, folderFromBAtA.getName());
		waitTillSynchronized(movedFolderFromBAtA, true);
		compareFiles(movedFolderFromBAtA, movedFolderFromBAtB);
		checkIndex(folderFromBAtA, folderFromBAtB, movedFolderFromBAtA, movedFolderFromBAtB);
	}

	private void checkIndex(File oldFileAtA, File oldFileAtB, File newFileAtA, File newFileAtB) throws GetFailedException,
			NoSessionException {
		UserProfile userProfileA = network.get(0).getSession().getProfileManager().readUserProfile();
		Index oldIndexAtA = userProfileA.getFileByPath(oldFileAtA, network.get(0).getSession().getRootFile());
		Index newIndexAtA = userProfileA.getFileByPath(newFileAtA, network.get(0).getSession().getRootFile());

		UserProfile userProfileB = network.get(1).getSession().getProfileManager().readUserProfile();
		Index oldIndexAtB = userProfileB.getFileByPath(oldFileAtB, network.get(1).getSession().getRootFile());
		Index newIndexAtB = userProfileB.getFileByPath(newFileAtB, network.get(1).getSession().getRootFile());

		// check if old indexes have been removed
		Assert.assertNull(oldIndexAtA);
		Assert.assertNull(oldIndexAtB);

		// check if content protection keys are the same
		Assert.assertTrue(newIndexAtA.getProtectionKeys().getPrivate().equals(newIndexAtB.getProtectionKeys().getPrivate()));
		Assert.assertTrue(newIndexAtA.getProtectionKeys().getPublic().equals(newIndexAtB.getProtectionKeys().getPublic()));

		// check if isShared flag is set
		Assert.assertTrue(newIndexAtA.isShared());
		Assert.assertTrue(newIndexAtB.isShared());

		// check write access
		Assert.assertTrue(newIndexAtA.canWrite());
		Assert.assertTrue(newIndexAtB.canWrite());

		// check user permissions at A
		Set<String> usersA = newIndexAtA.getCalculatedUserList();
		Assert.assertEquals(2, usersA.size());
		Assert.assertTrue(usersA.contains(userA.getUserId()));
		Assert.assertTrue(usersA.contains(userB.getUserId()));

		// check user permissions at A
		Set<String> usersB = newIndexAtB.getCalculatedUserList();
		Assert.assertEquals(2, usersB.size());
		Assert.assertTrue(usersB.contains(userA.getUserId()));
		Assert.assertTrue(usersB.contains(userB.getUserId()));

		// check user permissions in case of a folder at A
		if (newFileAtA.isDirectory()) {
			Assert.assertTrue(newIndexAtA.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) newIndexAtA).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.WRITE)));
		} else {
			Assert.assertTrue(newIndexAtA.isFile());
		}

		// check user permissions in case of a folder at B
		if (newFileAtB.isDirectory()) {
			Assert.assertTrue(newIndexAtB.isFolder());
			Set<UserPermission> permissions = ((FolderIndex) newIndexAtB).getCalculatedUserPermissions();
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.contains(new UserPermission(userA.getUserId(), PermissionType.WRITE)));
			Assert.assertTrue(permissions.contains(new UserPermission(userB.getUserId(), PermissionType.WRITE)));
		} else {
			Assert.assertTrue(newIndexAtB.isFile());
		}
	}
}
