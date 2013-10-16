/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>User Message Queue</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.UserMessageQueue#getQueue <em>Queue</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getUserMessageQueue()
 * @model
 * @generated
 */
public interface UserMessageQueue extends EObject {
	/**
	 * Returns the value of the '<em><b>Queue</b></em>' reference list.
	 * The list contents are of type {@link org.hive2hive.core.model.UserMessage}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Queue</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Queue</em>' reference list.
	 * @see org.hive2hive.core.model.ModelPackage#getUserMessageQueue_Queue()
	 * @model
	 * @generated
	 */
	EList<UserMessage> getQueue();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" required="true"
	 * @generated
	 */
	UserMessage getNext();

} // UserMessageQueue
