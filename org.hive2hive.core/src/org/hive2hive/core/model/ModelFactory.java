/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.hive2hive.core.model.ModelPackage
 * @generated
 */
public interface ModelFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelFactory eINSTANCE = org.hive2hive.core.model.impl.ModelFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Meta File</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Meta File</em>'.
	 * @generated
	 */
	MetaFile createMetaFile();

	/**
	 * Returns a new object of class '<em>Version</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Version</em>'.
	 * @generated
	 */
	Version createVersion();

	/**
	 * Returns a new object of class '<em>User Permission</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>User Permission</em>'.
	 * @generated
	 */
	UserPermission createUserPermission();

	/**
	 * Returns a new object of class '<em>Meta Folder</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Meta Folder</em>'.
	 * @generated
	 */
	MetaFolder createMetaFolder();

	/**
	 * Returns a new object of class '<em>Chunk</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Chunk</em>'.
	 * @generated
	 */
	Chunk createChunk();

	/**
	 * Returns a new object of class '<em>User Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>User Profile</em>'.
	 * @generated
	 */
	UserProfile createUserProfile();

	/**
	 * Returns a new object of class '<em>File Tree</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>File Tree</em>'.
	 * @generated
	 */
	FileTree createFileTree();

	/**
	 * Returns a new object of class '<em>File Tree Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>File Tree Node</em>'.
	 * @generated
	 */
	FileTreeNode createFileTreeNode();

	/**
	 * Returns a new object of class '<em>Online Peer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Online Peer</em>'.
	 * @generated
	 */
	OnlinePeer createOnlinePeer();

	/**
	 * Returns a new object of class '<em>Locations</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Locations</em>'.
	 * @generated
	 */
	Locations createLocations();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ModelPackage getModelPackage();

} //ModelFactory
