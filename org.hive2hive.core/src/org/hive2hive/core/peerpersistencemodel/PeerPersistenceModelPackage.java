/**
 */
package org.hive2hive.core.peerpersistencemodel;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;

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
 * @see org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelFactory
 * @model kind="package"
 * @generated
 */
public interface PeerPersistenceModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "peerpersistencemodel";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://peerpersistencemodel/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "peerpersistencemodel";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	PeerPersistenceModelPackage eINSTANCE = org.hive2hive.core.peerpersistencemodel.impl.PeerPersistenceModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.hive2hive.core.peerpersistencemodel.impl.PeerMemoryImpl <em>Peer Memory</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.hive2hive.core.peerpersistencemodel.impl.PeerMemoryImpl
	 * @see org.hive2hive.core.peerpersistencemodel.impl.PeerPersistenceModelPackageImpl#getPeerMemory()
	 * @generated
	 */
	int PEER_MEMORY = 0;

	/**
	 * The feature id for the '<em><b>Key Pair</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PEER_MEMORY__KEY_PAIR = 0;

	/**
	 * The number of structural features of the '<em>Peer Memory</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PEER_MEMORY_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '<em>Key Pair</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.security.KeyPair
	 * @see org.hive2hive.core.peerpersistencemodel.impl.PeerPersistenceModelPackageImpl#getKeyPair()
	 * @generated
	 */
	int KEY_PAIR = 1;


	/**
	 * Returns the meta object for class '{@link org.hive2hive.core.peerpersistencemodel.PeerMemory <em>Peer Memory</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Peer Memory</em>'.
	 * @see org.hive2hive.core.peerpersistencemodel.PeerMemory
	 * @generated
	 */
	EClass getPeerMemory();

	/**
	 * Returns the meta object for the attribute '{@link org.hive2hive.core.peerpersistencemodel.PeerMemory#getKeyPair <em>Key Pair</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key Pair</em>'.
	 * @see org.hive2hive.core.peerpersistencemodel.PeerMemory#getKeyPair()
	 * @see #getPeerMemory()
	 * @generated
	 */
	EAttribute getPeerMemory_KeyPair();

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
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	PeerPersistenceModelFactory getPeerPersistenceModelFactory();

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
		 * The meta object literal for the '{@link org.hive2hive.core.peerpersistencemodel.impl.PeerMemoryImpl <em>Peer Memory</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.hive2hive.core.peerpersistencemodel.impl.PeerMemoryImpl
		 * @see org.hive2hive.core.peerpersistencemodel.impl.PeerPersistenceModelPackageImpl#getPeerMemory()
		 * @generated
		 */
		EClass PEER_MEMORY = eINSTANCE.getPeerMemory();

		/**
		 * The meta object literal for the '<em><b>Key Pair</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PEER_MEMORY__KEY_PAIR = eINSTANCE.getPeerMemory_KeyPair();

		/**
		 * The meta object literal for the '<em>Key Pair</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.security.KeyPair
		 * @see org.hive2hive.core.peerpersistencemodel.impl.PeerPersistenceModelPackageImpl#getKeyPair()
		 * @generated
		 */
		EDataType KEY_PAIR = eINSTANCE.getKeyPair();

	}

} //PeerPersistenceModelPackage
