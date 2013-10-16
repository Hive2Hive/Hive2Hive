/**
 */
package org.hive2hive.core.model.impl;

import java.util.Collection;

import net.tomp2p.peers.PeerAddress;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.hive2hive.core.model.ModelPackage;
import org.hive2hive.core.model.OnlinePeer;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Online Peer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.impl.OnlinePeerImpl#getPeerAddress <em>Peer Address</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.OnlinePeerImpl#isMaster <em>Master</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OnlinePeerImpl extends MinimalEObjectImpl.Container implements OnlinePeer {
	/**
	 * The cached value of the '{@link #getPeerAddress() <em>Peer Address</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPeerAddress()
	 * @generated
	 * @ordered
	 */
	protected EList<PeerAddress> peerAddress;

	/**
	 * The default value of the '{@link #isMaster() <em>Master</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMaster()
	 * @generated
	 * @ordered
	 */
	protected static final boolean MASTER_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isMaster() <em>Master</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMaster()
	 * @generated
	 * @ordered
	 */
	protected boolean master = MASTER_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OnlinePeerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.ONLINE_PEER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<PeerAddress> getPeerAddress() {
		if (peerAddress == null) {
			peerAddress = new EDataTypeUniqueEList<PeerAddress>(PeerAddress.class, this, ModelPackage.ONLINE_PEER__PEER_ADDRESS);
		}
		return peerAddress;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isMaster() {
		return master;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMaster(boolean newMaster) {
		boolean oldMaster = master;
		master = newMaster;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ONLINE_PEER__MASTER, oldMaster, master));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.ONLINE_PEER__PEER_ADDRESS:
				return getPeerAddress();
			case ModelPackage.ONLINE_PEER__MASTER:
				return isMaster();
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
			case ModelPackage.ONLINE_PEER__PEER_ADDRESS:
				getPeerAddress().clear();
				getPeerAddress().addAll((Collection<? extends PeerAddress>)newValue);
				return;
			case ModelPackage.ONLINE_PEER__MASTER:
				setMaster((Boolean)newValue);
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
			case ModelPackage.ONLINE_PEER__PEER_ADDRESS:
				getPeerAddress().clear();
				return;
			case ModelPackage.ONLINE_PEER__MASTER:
				setMaster(MASTER_EDEFAULT);
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
			case ModelPackage.ONLINE_PEER__PEER_ADDRESS:
				return peerAddress != null && !peerAddress.isEmpty();
			case ModelPackage.ONLINE_PEER__MASTER:
				return master != MASTER_EDEFAULT;
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
		result.append(" (PeerAddress: ");
		result.append(peerAddress);
		result.append(", master: ");
		result.append(master);
		result.append(')');
		return result.toString();
	}

} //OnlinePeerImpl
