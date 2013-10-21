/**
 */
package org.hive2hive.core.peerpersistencemodel;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelPackage
 * @generated
 */
public interface PeerPersistenceModelFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	PeerPersistenceModelFactory eINSTANCE = org.hive2hive.core.peerpersistencemodel.impl.PeerPersistenceModelFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Peer Memory</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Peer Memory</em>'.
	 * @generated
	 */
	PeerMemory createPeerMemory();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	PeerPersistenceModelPackage getPeerPersistenceModelPackage();

} //PeerPersistenceModelFactory
