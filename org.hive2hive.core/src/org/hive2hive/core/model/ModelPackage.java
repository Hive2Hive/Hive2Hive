/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.hive2hive.core.model.ModelFactory
 * @model kind="package"
 * @generated
 */
public interface ModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://model/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "model";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelPackage eINSTANCE = org.hive2hive.core.model.impl.ModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.MetaDocumentImpl <em>Meta Document</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.MetaDocumentImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getMetaDocument()
	 * @generated
	 */
	int META_DOCUMENT = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_DOCUMENT__ID = 0;

	/**
	 * The number of structural features of the '<em>Meta Document</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_DOCUMENT_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.MetaFileImpl <em>Meta File</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.MetaFileImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getMetaFile()
	 * @generated
	 */
	int META_FILE = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FILE__ID = META_DOCUMENT__ID;

	/**
	 * The feature id for the '<em><b>Versions</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FILE__VERSIONS = META_DOCUMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Meta File</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FILE_FEATURE_COUNT = META_DOCUMENT_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.VersionImpl <em>Version</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.VersionImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getVersion()
	 * @generated
	 */
	int VERSION = 1;

	/**
	 * The feature id for the '<em><b>Chunks</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__CHUNKS = 0;

	/**
	 * The feature id for the '<em><b>Counter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__COUNTER = 1;

	/**
	 * The feature id for the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__SIZE = 2;

	/**
	 * The feature id for the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION__DATE = 3;

	/**
	 * The number of structural features of the '<em>Version</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION_FEATURE_COUNT = 4;


	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.UserPermissionImpl <em>User Permission</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.UserPermissionImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getUserPermission()
	 * @generated
	 */
	int USER_PERMISSION = 2;

	/**
	 * The feature id for the '<em><b>Userid</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PERMISSION__USERID = 0;

	/**
	 * The feature id for the '<em><b>Permission</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PERMISSION__PERMISSION = 1;

	/**
	 * The number of structural features of the '<em>User Permission</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PERMISSION_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.MetaFolderImpl <em>Meta Folder</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.MetaFolderImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getMetaFolder()
	 * @generated
	 */
	int META_FOLDER = 4;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FOLDER__ID = META_DOCUMENT__ID;

	/**
	 * The feature id for the '<em><b>Content</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FOLDER__CONTENT = META_DOCUMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>User Permission</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FOLDER__USER_PERMISSION = META_DOCUMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Meta Folder</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FOLDER_FEATURE_COUNT = META_DOCUMENT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.ChunkImpl <em>Chunk</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.ChunkImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getChunk()
	 * @generated
	 */
	int CHUNK = 5;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHUNK__DATA = 0;

	/**
	 * The number of structural features of the '<em>Chunk</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHUNK_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.UserProfileImpl <em>User Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.UserProfileImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getUserProfile()
	 * @generated
	 */
	int USER_PROFILE = 6;

	/**
	 * The feature id for the '<em><b>File Tree</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PROFILE__FILE_TREE = 0;

	/**
	 * The feature id for the '<em><b>Signature Keys</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PROFILE__SIGNATURE_KEYS = 1;

	/**
	 * The feature id for the '<em><b>User Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PROFILE__USER_ID = 2;

	/**
	 * The number of structural features of the '<em>User Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int USER_PROFILE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.FileTreeImpl <em>File Tree</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.FileTreeImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getFileTree()
	 * @generated
	 */
	int FILE_TREE = 7;

	/**
	 * The feature id for the '<em><b>Children</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_TREE__CHILDREN = 0;

	/**
	 * The number of structural features of the '<em>File Tree</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_TREE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.FileTreeNodeImpl <em>File Tree Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.FileTreeNodeImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getFileTreeNode()
	 * @generated
	 */
	int FILE_TREE_NODE = 8;

	/**
	 * The feature id for the '<em><b>Key Pair</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_TREE_NODE__KEY_PAIR = 0;

	/**
	 * The feature id for the '<em><b>Domain Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_TREE_NODE__DOMAIN_KEY = 1;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_TREE_NODE__PARENT = 2;

	/**
	 * The number of structural features of the '<em>File Tree Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILE_TREE_NODE_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.OnlinePeerImpl <em>Online Peer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.OnlinePeerImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getOnlinePeer()
	 * @generated
	 */
	int ONLINE_PEER = 9;

	/**
	 * The feature id for the '<em><b>Peer Address</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ONLINE_PEER__PEER_ADDRESS = 0;

	/**
	 * The feature id for the '<em><b>Master</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ONLINE_PEER__MASTER = 1;

	/**
	 * The number of structural features of the '<em>Online Peer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ONLINE_PEER_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.LocationsImpl <em>Locations</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.LocationsImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getLocations()
	 * @generated
	 */
	int LOCATIONS = 10;

	/**
	 * The feature id for the '<em><b>Online Peers</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATIONS__ONLINE_PEERS = 0;

	/**
	 * The number of structural features of the '<em>Locations</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATIONS_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.hive2hive.core.model.Permission <em>Permission</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.Permission
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getPermission()
	 * @generated
	 */
	int PERMISSION = 11;


	/**
	 * The meta object id for the '<em>Public Key</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.security.PublicKey
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getPublicKey()
	 * @generated
	 */
	int PUBLIC_KEY = 12;

	/**
	 * The meta object id for the '<em>Private Key</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.security.PrivateKey
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getPrivateKey()
	 * @generated
	 */
	int PRIVATE_KEY = 13;

	/**
	 * The meta object id for the '<em>Key Pair</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.security.KeyPair
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getKeyPair()
	 * @generated
	 */
	int KEY_PAIR = 14;

	/**
	 * The meta object id for the '<em>Peer Address</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see net.tomp2p.peers.PeerAddress
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getPeerAddress()
	 * @generated
	 */
	int PEER_ADDRESS = 15;


	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.MetaFile <em>Meta File</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Meta File</em>'.
	 * @see org.hive2hive.core.model.MetaFile
	 * @generated
	 */
	EClass getMetaFile();

	/**
	 * Returns the meta object for the reference list '{@link org.hive2hive.core.model.MetaFile#getVersions <em>Versions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Versions</em>'.
	 * @see org.hive2hive.core.model.MetaFile#getVersions()
	 * @see #getMetaFile()
	 * @generated
	 */
	EReference getMetaFile_Versions();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.Version <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Version</em>'.
	 * @see org.hive2hive.core.model.Version
	 * @generated
	 */
	EClass getVersion();

	/**
	 * Returns the meta object for the reference list '{@link org.hive2hive.core.model.Version#getChunks <em>Chunks</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Chunks</em>'.
	 * @see org.hive2hive.core.model.Version#getChunks()
	 * @see #getVersion()
	 * @generated
	 */
	EReference getVersion_Chunks();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.Version#getCounter <em>Counter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Counter</em>'.
	 * @see org.hive2hive.core.model.Version#getCounter()
	 * @see #getVersion()
	 * @generated
	 */
	EAttribute getVersion_Counter();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.Version#getSize <em>Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Size</em>'.
	 * @see org.hive2hive.core.model.Version#getSize()
	 * @see #getVersion()
	 * @generated
	 */
	EAttribute getVersion_Size();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.Version#getDate <em>Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Date</em>'.
	 * @see org.hive2hive.core.model.Version#getDate()
	 * @see #getVersion()
	 * @generated
	 */
	EAttribute getVersion_Date();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.UserPermission <em>User Permission</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>User Permission</em>'.
	 * @see org.hive2hive.core.model.UserPermission
	 * @generated
	 */
	EClass getUserPermission();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.UserPermission#getUserid <em>Userid</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Userid</em>'.
	 * @see org.hive2hive.core.model.UserPermission#getUserid()
	 * @see #getUserPermission()
	 * @generated
	 */
	EAttribute getUserPermission_Userid();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.UserPermission#getPermission <em>Permission</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Permission</em>'.
	 * @see org.hive2hive.core.model.UserPermission#getPermission()
	 * @see #getUserPermission()
	 * @generated
	 */
	EAttribute getUserPermission_Permission();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.MetaDocument <em>Meta Document</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Meta Document</em>'.
	 * @see org.hive2hive.core.model.MetaDocument
	 * @generated
	 */
	EClass getMetaDocument();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.MetaDocument#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.hive2hive.core.model.MetaDocument#getId()
	 * @see #getMetaDocument()
	 * @generated
	 */
	EAttribute getMetaDocument_Id();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.MetaFolder <em>Meta Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Meta Folder</em>'.
	 * @see org.hive2hive.core.model.MetaFolder
	 * @generated
	 */
	EClass getMetaFolder();

	/**
	 * Returns the meta object for the reference list '{@link org.hive2hive.core.model.MetaFolder#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Content</em>'.
	 * @see org.hive2hive.core.model.MetaFolder#getContent()
	 * @see #getMetaFolder()
	 * @generated
	 */
	EReference getMetaFolder_Content();

	/**
	 * Returns the meta object for the reference list '{@link org.hive2hive.core.model.MetaFolder#getUserPermission <em>User Permission</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>User Permission</em>'.
	 * @see org.hive2hive.core.model.MetaFolder#getUserPermission()
	 * @see #getMetaFolder()
	 * @generated
	 */
	EReference getMetaFolder_UserPermission();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.Chunk <em>Chunk</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Chunk</em>'.
	 * @see org.hive2hive.core.model.Chunk
	 * @generated
	 */
	EClass getChunk();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.Chunk#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data</em>'.
	 * @see org.hive2hive.core.model.Chunk#getData()
	 * @see #getChunk()
	 * @generated
	 */
	EAttribute getChunk_Data();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.UserProfile <em>User Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>User Profile</em>'.
	 * @see org.hive2hive.core.model.UserProfile
	 * @generated
	 */
	EClass getUserProfile();

	/**
	 * Returns the meta object for the reference '{@link org.hive2hive.core.model.UserProfile#getFileTree <em>File Tree</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>File Tree</em>'.
	 * @see org.hive2hive.core.model.UserProfile#getFileTree()
	 * @see #getUserProfile()
	 * @generated
	 */
	EReference getUserProfile_FileTree();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.UserProfile#getSignatureKeys <em>Signature Keys</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Signature Keys</em>'.
	 * @see org.hive2hive.core.model.UserProfile#getSignatureKeys()
	 * @see #getUserProfile()
	 * @generated
	 */
	EAttribute getUserProfile_SignatureKeys();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.UserProfile#getUserId <em>User Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>User Id</em>'.
	 * @see org.hive2hive.core.model.UserProfile#getUserId()
	 * @see #getUserProfile()
	 * @generated
	 */
	EAttribute getUserProfile_UserId();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.FileTree <em>File Tree</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>File Tree</em>'.
	 * @see org.hive2hive.core.model.FileTree
	 * @generated
	 */
	EClass getFileTree();

	/**
	 * Returns the meta object for the reference list '{@link org.hive2hive.core.model.FileTree#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Children</em>'.
	 * @see org.hive2hive.core.model.FileTree#getChildren()
	 * @see #getFileTree()
	 * @generated
	 */
	EReference getFileTree_Children();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.FileTreeNode <em>File Tree Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>File Tree Node</em>'.
	 * @see org.hive2hive.core.model.FileTreeNode
	 * @generated
	 */
	EClass getFileTreeNode();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.FileTreeNode#getKeyPair <em>Key Pair</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key Pair</em>'.
	 * @see org.hive2hive.core.model.FileTreeNode#getKeyPair()
	 * @see #getFileTreeNode()
	 * @generated
	 */
	EAttribute getFileTreeNode_KeyPair();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.FileTreeNode#getDomainKey <em>Domain Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Domain Key</em>'.
	 * @see org.hive2hive.core.model.FileTreeNode#getDomainKey()
	 * @see #getFileTreeNode()
	 * @generated
	 */
	EAttribute getFileTreeNode_DomainKey();

	/**
	 * Returns the meta object for the reference '{@link org.hive2hive.core.model.FileTreeNode#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Parent</em>'.
	 * @see org.hive2hive.core.model.FileTreeNode#getParent()
	 * @see #getFileTreeNode()
	 * @generated
	 */
	EReference getFileTreeNode_Parent();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.OnlinePeer <em>Online Peer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Online Peer</em>'.
	 * @see org.hive2hive.core.model.OnlinePeer
	 * @generated
	 */
	EClass getOnlinePeer();

	/**
	 * Returns the meta object for the attribute list '{@link org.hive2hive.core.model.OnlinePeer#getPeerAddress <em>Peer Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Peer Address</em>'.
	 * @see org.hive2hive.core.model.OnlinePeer#getPeerAddress()
	 * @see #getOnlinePeer()
	 * @generated
	 */
	EAttribute getOnlinePeer_PeerAddress();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.model.OnlinePeer#isMaster <em>Master</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Master</em>'.
	 * @see org.hive2hive.core.model.OnlinePeer#isMaster()
	 * @see #getOnlinePeer()
	 * @generated
	 */
	EAttribute getOnlinePeer_Master();

	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.model.Locations <em>Locations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Locations</em>'.
	 * @see org.hive2hive.core.model.Locations
	 * @generated
	 */
	EClass getLocations();

	/**
	 * Returns the meta object for the reference '{@link org.hive2hive.core.model.Locations#getOnlinePeers <em>Online Peers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Online Peers</em>'.
	 * @see org.hive2hive.core.model.Locations#getOnlinePeers()
	 * @see #getLocations()
	 * @generated
	 */
	EReference getLocations_OnlinePeers();

	/**
	 * Returns the meta object for enum '{@link org.hive2hive.core.model.Permission <em>Permission</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Permission</em>'.
	 * @see org.hive2hive.core.model.Permission
	 * @generated
	 */
	EEnum getPermission();

	/**
	 * Returns the meta object for data type '{@link java.security.PublicKey <em>Public Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Public Key</em>'.
	 * @see java.security.PublicKey
	 * @model instanceClass="java.security.PublicKey"
	 * @generated
	 */
	EDataType getPublicKey();

	/**
	 * Returns the meta object for data type '{@link java.security.PrivateKey <em>Private Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Private Key</em>'.
	 * @see java.security.PrivateKey
	 * @model instanceClass="java.security.PrivateKey"
	 * @generated
	 */
	EDataType getPrivateKey();

	/**
	 * Returns the meta object for data type '{@link java.security.KeyPair <em>Key Pair</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Key Pair</em>'.
	 * @see java.security.KeyPair
	 * @model instanceClass="java.security.KeyPair"
	 * @generated
	 */
	EDataType getKeyPair();

	/**
	 * Returns the meta object for data type '{@link net.tomp2p.peers.PeerAddress <em>Peer Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Peer Address</em>'.
	 * @see net.tomp2p.peers.PeerAddress
	 * @model instanceClass="net.tomp2p.peers.PeerAddress"
	 * @generated
	 */
	EDataType getPeerAddress();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelFactory getModelFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.MetaFileImpl <em>Meta File</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.MetaFileImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getMetaFile()
		 * @generated
		 */
		EClass META_FILE = eINSTANCE.getMetaFile();

		/**
		 * The meta object literal for the '<em><b>Versions</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference META_FILE__VERSIONS = eINSTANCE.getMetaFile_Versions();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.VersionImpl <em>Version</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.VersionImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getVersion()
		 * @generated
		 */
		EClass VERSION = eINSTANCE.getVersion();

		/**
		 * The meta object literal for the '<em><b>Chunks</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VERSION__CHUNKS = eINSTANCE.getVersion_Chunks();

		/**
		 * The meta object literal for the '<em><b>Counter</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VERSION__COUNTER = eINSTANCE.getVersion_Counter();

		/**
		 * The meta object literal for the '<em><b>Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VERSION__SIZE = eINSTANCE.getVersion_Size();

		/**
		 * The meta object literal for the '<em><b>Date</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VERSION__DATE = eINSTANCE.getVersion_Date();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.UserPermissionImpl <em>User Permission</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.UserPermissionImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getUserPermission()
		 * @generated
		 */
		EClass USER_PERMISSION = eINSTANCE.getUserPermission();

		/**
		 * The meta object literal for the '<em><b>Userid</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute USER_PERMISSION__USERID = eINSTANCE.getUserPermission_Userid();

		/**
		 * The meta object literal for the '<em><b>Permission</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute USER_PERMISSION__PERMISSION = eINSTANCE.getUserPermission_Permission();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.MetaDocumentImpl <em>Meta Document</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.MetaDocumentImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getMetaDocument()
		 * @generated
		 */
		EClass META_DOCUMENT = eINSTANCE.getMetaDocument();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute META_DOCUMENT__ID = eINSTANCE.getMetaDocument_Id();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.MetaFolderImpl <em>Meta Folder</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.MetaFolderImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getMetaFolder()
		 * @generated
		 */
		EClass META_FOLDER = eINSTANCE.getMetaFolder();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference META_FOLDER__CONTENT = eINSTANCE.getMetaFolder_Content();

		/**
		 * The meta object literal for the '<em><b>User Permission</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference META_FOLDER__USER_PERMISSION = eINSTANCE.getMetaFolder_UserPermission();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.ChunkImpl <em>Chunk</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.ChunkImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getChunk()
		 * @generated
		 */
		EClass CHUNK = eINSTANCE.getChunk();

		/**
		 * The meta object literal for the '<em><b>Data</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CHUNK__DATA = eINSTANCE.getChunk_Data();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.UserProfileImpl <em>User Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.UserProfileImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getUserProfile()
		 * @generated
		 */
		EClass USER_PROFILE = eINSTANCE.getUserProfile();

		/**
		 * The meta object literal for the '<em><b>File Tree</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference USER_PROFILE__FILE_TREE = eINSTANCE.getUserProfile_FileTree();

		/**
		 * The meta object literal for the '<em><b>Signature Keys</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute USER_PROFILE__SIGNATURE_KEYS = eINSTANCE.getUserProfile_SignatureKeys();

		/**
		 * The meta object literal for the '<em><b>User Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute USER_PROFILE__USER_ID = eINSTANCE.getUserProfile_UserId();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.FileTreeImpl <em>File Tree</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.FileTreeImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getFileTree()
		 * @generated
		 */
		EClass FILE_TREE = eINSTANCE.getFileTree();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FILE_TREE__CHILDREN = eINSTANCE.getFileTree_Children();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.FileTreeNodeImpl <em>File Tree Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.FileTreeNodeImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getFileTreeNode()
		 * @generated
		 */
		EClass FILE_TREE_NODE = eINSTANCE.getFileTreeNode();

		/**
		 * The meta object literal for the '<em><b>Key Pair</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILE_TREE_NODE__KEY_PAIR = eINSTANCE.getFileTreeNode_KeyPair();

		/**
		 * The meta object literal for the '<em><b>Domain Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FILE_TREE_NODE__DOMAIN_KEY = eINSTANCE.getFileTreeNode_DomainKey();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FILE_TREE_NODE__PARENT = eINSTANCE.getFileTreeNode_Parent();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.OnlinePeerImpl <em>Online Peer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.OnlinePeerImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getOnlinePeer()
		 * @generated
		 */
		EClass ONLINE_PEER = eINSTANCE.getOnlinePeer();

		/**
		 * The meta object literal for the '<em><b>Peer Address</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ONLINE_PEER__PEER_ADDRESS = eINSTANCE.getOnlinePeer_PeerAddress();

		/**
		 * The meta object literal for the '<em><b>Master</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ONLINE_PEER__MASTER = eINSTANCE.getOnlinePeer_Master();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.impl.LocationsImpl <em>Locations</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.impl.LocationsImpl
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getLocations()
		 * @generated
		 */
		EClass LOCATIONS = eINSTANCE.getLocations();

		/**
		 * The meta object literal for the '<em><b>Online Peers</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LOCATIONS__ONLINE_PEERS = eINSTANCE.getLocations_OnlinePeers();

		/**
		 * The meta object literal for the '{@link org.hive2hive.core.model.Permission <em>Permission</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.model.Permission
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getPermission()
		 * @generated
		 */
		EEnum PERMISSION = eINSTANCE.getPermission();

		/**
		 * The meta object literal for the '<em>Public Key</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.security.PublicKey
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getPublicKey()
		 * @generated
		 */
		EDataType PUBLIC_KEY = eINSTANCE.getPublicKey();

		/**
		 * The meta object literal for the '<em>Private Key</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.security.PrivateKey
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getPrivateKey()
		 * @generated
		 */
		EDataType PRIVATE_KEY = eINSTANCE.getPrivateKey();

		/**
		 * The meta object literal for the '<em>Key Pair</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.security.KeyPair
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getKeyPair()
		 * @generated
		 */
		EDataType KEY_PAIR = eINSTANCE.getKeyPair();

		/**
		 * The meta object literal for the '<em>Peer Address</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see net.tomp2p.peers.PeerAddress
		 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getPeerAddress()
		 * @generated
		 */
		EDataType PEER_ADDRESS = eINSTANCE.getPeerAddress();

	}

} //ModelPackage
