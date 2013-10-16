/**
 */
package org.hive2hive.core.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.model.ModelPackage;
import org.hive2hive.core.model.OnlinePeer;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Locations</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.impl.LocationsImpl#getOnlinePeers <em>Online Peers</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class LocationsImpl extends MinimalEObjectImpl.Container implements Locations {
	/**
	 * The cached value of the '{@link #getOnlinePeers() <em>Online Peers</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOnlinePeers()
	 * @generated
	 * @ordered
	 */
	protected OnlinePeer onlinePeers;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LocationsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.LOCATIONS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OnlinePeer getOnlinePeers() {
		if (onlinePeers != null && onlinePeers.eIsProxy()) {
			InternalEObject oldOnlinePeers = (InternalEObject)onlinePeers;
			onlinePeers = (OnlinePeer)eResolveProxy(oldOnlinePeers);
			if (onlinePeers != oldOnlinePeers) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.LOCATIONS__ONLINE_PEERS, oldOnlinePeers, onlinePeers));
			}
		}
		return onlinePeers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OnlinePeer basicGetOnlinePeers() {
		return onlinePeers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOnlinePeers(OnlinePeer newOnlinePeers) {
		OnlinePeer oldOnlinePeers = onlinePeers;
		onlinePeers = newOnlinePeers;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.LOCATIONS__ONLINE_PEERS, oldOnlinePeers, onlinePeers));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.LOCATIONS__ONLINE_PEERS:
				if (resolve) return getOnlinePeers();
				return basicGetOnlinePeers();
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
			case ModelPackage.LOCATIONS__ONLINE_PEERS:
				setOnlinePeers((OnlinePeer)newValue);
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
			case ModelPackage.LOCATIONS__ONLINE_PEERS:
				setOnlinePeers((OnlinePeer)null);
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
			case ModelPackage.LOCATIONS__ONLINE_PEERS:
				return onlinePeers != null;
		}
		return super.eIsSet(featureID);
	}

} //LocationsImpl
