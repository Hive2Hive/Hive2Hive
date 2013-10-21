/**
 */
package org.hive2hive.core.peerpersistencemodel.impl;

import java.security.KeyPair;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.hive2hive.core.peerpersistencemodel.PeerMemory;
import org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelFactory;
import org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PeerPersistenceModelPackageImpl extends EPackageImpl implements PeerPersistenceModelPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass peerMemoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType keyPairEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private PeerPersistenceModelPackageImpl() {
		super(eNS_URI, PeerPersistenceModelFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link PeerPersistenceModelPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static PeerPersistenceModelPackage init() {
		if (isInited) return (PeerPersistenceModelPackage)EPackage.Registry.INSTANCE.getEPackage(PeerPersistenceModelPackage.eNS_URI);

		// Obtain or create and register package
		PeerPersistenceModelPackageImpl thePeerPersistenceModelPackage = (PeerPersistenceModelPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof PeerPersistenceModelPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new PeerPersistenceModelPackageImpl());

		isInited = true;

		// Create package meta-data objects
		thePeerPersistenceModelPackage.createPackageContents();

		// Initialize created meta-data
		thePeerPersistenceModelPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		thePeerPersistenceModelPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(PeerPersistenceModelPackage.eNS_URI, thePeerPersistenceModelPackage);
		return thePeerPersistenceModelPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPeerMemory() {
		return peerMemoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPeerMemory_KeyPair() {
		return (EAttribute)peerMemoryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getKeyPair() {
		return keyPairEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PeerPersistenceModelFactory getPeerPersistenceModelFactory() {
		return (PeerPersistenceModelFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		peerMemoryEClass = createEClass(PEER_MEMORY);
		createEAttribute(peerMemoryEClass, PEER_MEMORY__KEY_PAIR);

		// Create data types
		keyPairEDataType = createEDataType(KEY_PAIR);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(peerMemoryEClass, PeerMemory.class, "PeerMemory", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPeerMemory_KeyPair(), this.getKeyPair(), "keyPair", null, 0, 1, PeerMemory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize data types
		initEDataType(keyPairEDataType, KeyPair.class, "KeyPair", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //PeerPersistenceModelPackageImpl
