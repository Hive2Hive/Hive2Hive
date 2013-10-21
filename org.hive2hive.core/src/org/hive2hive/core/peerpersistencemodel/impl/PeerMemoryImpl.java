/**
 */
package org.hive2hive.core.peerpersistencemodel.impl;

import java.security.KeyPair;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.hive2hive.core.peerpersistencemodel.PeerMemory;
import org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Peer Memory</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.peerpersistencemodel.impl.PeerMemoryImpl#getKeyPair <em>Key Pair</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PeerMemoryImpl extends EObjectImpl implements PeerMemory {
	/**
	 * The default value of the '{@link #getKeyPair() <em>Key Pair</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeyPair()
	 * @generated
	 * @ordered
	 */
	protected static final KeyPair KEY_PAIR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getKeyPair() <em>Key Pair</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeyPair()
	 * @generated
	 * @ordered
	 */
	protected KeyPair keyPair = KEY_PAIR_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PeerMemoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeerPersistenceModelPackage.Literals.PEER_MEMORY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public KeyPair getKeyPair() {
		return keyPair;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setKeyPair(KeyPair newKeyPair) {
		KeyPair oldKeyPair = keyPair;
		keyPair = newKeyPair;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeerPersistenceModelPackage.PEER_MEMORY__KEY_PAIR, oldKeyPair, keyPair));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeerPersistenceModelPackage.PEER_MEMORY__KEY_PAIR:
				return getKeyPair();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PeerPersistenceModelPackage.PEER_MEMORY__KEY_PAIR:
				setKeyPair((KeyPair)newValue);
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
			case PeerPersistenceModelPackage.PEER_MEMORY__KEY_PAIR:
				setKeyPair(KEY_PAIR_EDEFAULT);
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
			case PeerPersistenceModelPackage.PEER_MEMORY__KEY_PAIR:
				return KEY_PAIR_EDEFAULT == null ? keyPair != null : !KEY_PAIR_EDEFAULT.equals(keyPair);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (keyPair: ");
		result.append(keyPair);
		result.append(')');
		return result.toString();
	}

} //PeerMemoryImpl
