/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Locations</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.Locations#getOnlinePeers <em>Online Peers</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getLocations()
 * @model
 * @generated
 */
public interface Locations extends EObject {
	/**
	 * Returns the value of the '<em><b>Online Peers</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Online Peers</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Online Peers</em>' reference.
	 * @see #setOnlinePeers(OnlinePeer)
	 * @see org.hive2hive.core.model.ModelPackage#getLocations_OnlinePeers()
	 * @model
	 * @generated
	 */
	OnlinePeer getOnlinePeers();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.Locations#getOnlinePeers <em>Online Peers</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Online Peers</em>' reference.
	 * @see #getOnlinePeers()
	 * @generated
	 */
	void setOnlinePeers(OnlinePeer value);

} // Locations
