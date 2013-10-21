/**
 */
package org.hive2hive.core.model;

import net.tomp2p.peers.PeerAddress;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>User Message</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.UserMessage#getId <em>Id</em>}</li>
 *   <li>{@link org.hive2hive.core.model.UserMessage#getSender <em>Sender</em>}</li>
 *   <li>{@link org.hive2hive.core.model.UserMessage#getOrigin <em>Origin</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getUserMessage()
 * @model abstract="true"
 * @generated
 */
public interface UserMessage extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.hive2hive.core.model.ModelPackage#getUserMessage_Id()
	 * @model required="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserMessage#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Sender</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sender</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sender</em>' attribute.
	 * @see #setSender(String)
	 * @see org.hive2hive.core.model.ModelPackage#getUserMessage_Sender()
	 * @model required="true"
	 * @generated
	 */
	String getSender();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserMessage#getSender <em>Sender</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sender</em>' attribute.
	 * @see #getSender()
	 * @generated
	 */
	void setSender(String value);

	/**
	 * Returns the value of the '<em><b>Origin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Origin</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Origin</em>' attribute.
	 * @see #setOrigin(PeerAddress)
	 * @see org.hive2hive.core.model.ModelPackage#getUserMessage_Origin()
	 * @model dataType="org.hive2hive.core.model.PeerAddress" required="true"
	 * @generated
	 */
	PeerAddress getOrigin();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserMessage#getOrigin <em>Origin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Origin</em>' attribute.
	 * @see #getOrigin()
	 * @generated
	 */
	void setOrigin(PeerAddress value);

} // UserMessage
