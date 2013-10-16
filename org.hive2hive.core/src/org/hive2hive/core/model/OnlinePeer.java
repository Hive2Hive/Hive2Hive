/**
 */
package org.hive2hive.core.model;

import net.tomp2p.peers.PeerAddress;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Online Peer</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.OnlinePeer#getPeerAddress <em>Peer Address</em>}</li>
 *   <li>{@link org.hive2hive.core.model.OnlinePeer#isMaster <em>Master</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getOnlinePeer()
 * @model
 * @generated
 */
public interface OnlinePeer extends EObject {
	/**
	 * Returns the value of the '<em><b>Peer Address</b></em>' attribute list.
	 * The list contents are of type {@link net.tomp2p.peers.PeerAddress}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Peer Address</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Peer Address</em>' attribute list.
	 * @see org.hive2hive.core.model.ModelPackage#getOnlinePeer_PeerAddress()
	 * @model dataType="org.hive2hive.core.model.PeerAddress"
	 * @generated
	 */
	EList<PeerAddress> getPeerAddress();

	/**
	 * Returns the value of the '<em><b>Master</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Master</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Master</em>' attribute.
	 * @see #setMaster(boolean)
	 * @see org.hive2hive.core.model.ModelPackage#getOnlinePeer_Master()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isMaster();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.OnlinePeer#isMaster <em>Master</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Master</em>' attribute.
	 * @see #isMaster()
	 * @generated
	 */
	void setMaster(boolean value);

} // OnlinePeer
