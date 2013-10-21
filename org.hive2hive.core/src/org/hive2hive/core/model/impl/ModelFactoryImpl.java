/**
 */
package org.hive2hive.core.model.impl;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import net.tomp2p.peers.PeerAddress;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.hive2hive.core.model.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelFactoryImpl extends EFactoryImpl implements ModelFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ModelFactory init() {
		try {
			ModelFactory theModelFactory = (ModelFactory)EPackage.Registry.INSTANCE.getEFactory("http://model/1.0"); 
			if (theModelFactory != null) {
				return theModelFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ModelFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case ModelPackage.META_FILE: return createMetaFile();
			case ModelPackage.VERSION: return createVersion();
			case ModelPackage.USER_PERMISSION: return createUserPermission();
			case ModelPackage.META_FOLDER: return createMetaFolder();
			case ModelPackage.CHUNK: return createChunk();
			case ModelPackage.USER_PROFILE: return createUserProfile();
			case ModelPackage.FILE_TREE: return createFileTree();
			case ModelPackage.FILE_TREE_NODE: return createFileTreeNode();
			case ModelPackage.ONLINE_PEER: return createOnlinePeer();
			case ModelPackage.LOCATIONS: return createLocations();
			case ModelPackage.USER_MESSAGE_QUEUE: return createUserMessageQueue();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case ModelPackage.PERMISSION:
				return createPermissionFromString(eDataType, initialValue);
			case ModelPackage.PUBLIC_KEY:
				return createPublicKeyFromString(eDataType, initialValue);
			case ModelPackage.PRIVATE_KEY:
				return createPrivateKeyFromString(eDataType, initialValue);
			case ModelPackage.KEY_PAIR:
				return createKeyPairFromString(eDataType, initialValue);
			case ModelPackage.PEER_ADDRESS:
				return createPeerAddressFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case ModelPackage.PERMISSION:
				return convertPermissionToString(eDataType, instanceValue);
			case ModelPackage.PUBLIC_KEY:
				return convertPublicKeyToString(eDataType, instanceValue);
			case ModelPackage.PRIVATE_KEY:
				return convertPrivateKeyToString(eDataType, instanceValue);
			case ModelPackage.KEY_PAIR:
				return convertKeyPairToString(eDataType, instanceValue);
			case ModelPackage.PEER_ADDRESS:
				return convertPeerAddressToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetaFile createMetaFile() {
		MetaFileImpl metaFile = new MetaFileImpl();
		return metaFile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Version createVersion() {
		VersionImpl version = new VersionImpl();
		return version;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UserPermission createUserPermission() {
		UserPermissionImpl userPermission = new UserPermissionImpl();
		return userPermission;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetaFolder createMetaFolder() {
		MetaFolderImpl metaFolder = new MetaFolderImpl();
		return metaFolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Chunk createChunk() {
		ChunkImpl chunk = new ChunkImpl();
		return chunk;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UserProfile createUserProfile() {
		UserProfileImpl userProfile = new UserProfileImpl();
		return userProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FileTree createFileTree() {
		FileTreeImpl fileTree = new FileTreeImpl();
		return fileTree;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FileTreeNode createFileTreeNode() {
		FileTreeNodeImpl fileTreeNode = new FileTreeNodeImpl();
		return fileTreeNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OnlinePeer createOnlinePeer() {
		OnlinePeerImpl onlinePeer = new OnlinePeerImpl();
		return onlinePeer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Locations createLocations() {
		LocationsImpl locations = new LocationsImpl();
		return locations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UserMessageQueue createUserMessageQueue() {
		UserMessageQueueImpl userMessageQueue = new UserMessageQueueImpl();
		return userMessageQueue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Permission createPermissionFromString(EDataType eDataType, String initialValue) {
		Permission result = Permission.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPermissionToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PublicKey createPublicKeyFromString(EDataType eDataType, String initialValue) {
		return (PublicKey)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPublicKeyToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PrivateKey createPrivateKeyFromString(EDataType eDataType, String initialValue) {
		return (PrivateKey)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPrivateKeyToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public KeyPair createKeyPairFromString(EDataType eDataType, String initialValue) {
		return (KeyPair)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertKeyPairToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PeerAddress createPeerAddressFromString(EDataType eDataType, String initialValue) {
		return (PeerAddress)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPeerAddressToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelPackage getModelPackage() {
		return (ModelPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ModelPackage getPackage() {
		return ModelPackage.eINSTANCE;
	}

} //ModelFactoryImpl
