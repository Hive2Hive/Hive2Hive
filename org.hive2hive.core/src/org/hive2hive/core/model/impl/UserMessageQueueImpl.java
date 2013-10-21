/**
 */
package org.hive2hive.core.model.impl;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.hive2hive.core.model.ModelPackage;
import org.hive2hive.core.model.UserMessage;
import org.hive2hive.core.model.UserMessageQueue;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>User Message Queue</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.impl.UserMessageQueueImpl#getQueue <em>Queue</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class UserMessageQueueImpl extends MinimalEObjectImpl.Container implements UserMessageQueue {
	/**
	 * The cached value of the '{@link #getQueue() <em>Queue</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQueue()
	 * @generated
	 * @ordered
	 */
	protected EList<UserMessage> queue;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UserMessageQueueImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.USER_MESSAGE_QUEUE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<UserMessage> getQueue() {
		if (queue == null) {
			queue = new EObjectResolvingEList<UserMessage>(UserMessage.class, this, ModelPackage.USER_MESSAGE_QUEUE__QUEUE);
		}
		return queue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UserMessage getNext() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.USER_MESSAGE_QUEUE__QUEUE:
				return getQueue();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelPackage.USER_MESSAGE_QUEUE__QUEUE:
				getQueue().clear();
				getQueue().addAll((Collection<? extends UserMessage>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ModelPackage.USER_MESSAGE_QUEUE__QUEUE:
				getQueue().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ModelPackage.USER_MESSAGE_QUEUE__QUEUE:
				return queue != null && !queue.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //UserMessageQueueImpl
