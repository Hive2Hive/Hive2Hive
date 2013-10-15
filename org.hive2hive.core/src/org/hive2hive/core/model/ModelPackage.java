/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.ecore.EClass;
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
	 * The meta object id for the '{@link org.hive2hive.core.model.impl.MetaFileImpl <em>Meta File</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.model.impl.MetaFileImpl
	 * @see org.hive2hive.core.model.impl.ModelPackageImpl#getMetaFile()
	 * @generated
	 */
	int META_FILE = 0;

	/**
	 * The feature id for the '<em><b>Versions</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FILE__VERSIONS = 0;

	/**
	 * The number of structural features of the '<em>Meta File</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int META_FILE_FEATURE_COUNT = 1;

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
	 * The number of structural features of the '<em>Version</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VERSION_FEATURE_COUNT = 0;


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

	}

} //ModelPackage
