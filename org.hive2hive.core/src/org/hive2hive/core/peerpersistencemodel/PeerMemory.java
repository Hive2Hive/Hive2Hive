/**
 */
package org.hive2hive.core.peerpersistencemodel;

import java.security.KeyPair;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Peer Memory</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.peerpersistencemodel.PeerMemory#getKeyPair <em>Key Pair</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelPackage#getPeerMemory()
 * @model
 * @generated
 */
public interface PeerMemory extends EObject {
	/**
	 * Returns the value of the '<em><b>Key Pair</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Key Pair</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Key Pair</em>' attribute.
	 * @see #setKeyPair(KeyPair)
	 * @see org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelPackage#getPeerMemory_KeyPair()
	 * @model dataType="org.hive2hive.core.peerpersistencemodel.KeyPair"
	 * @generated
	 */
	KeyPair getKeyPair();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.peerpersistencemodel.PeerMemory#getKeyPair <em>Key Pair</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Key Pair</em>' attribute.
	 * @see #getKeyPair()
	 * @generated
	 */
	void setKeyPair(KeyPair value);

} // PeerMemory
