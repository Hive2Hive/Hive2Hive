/**
 */
package org.hive2hive.core.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.hive2hive.core.model.ModelPackage;
import org.hive2hive.core.model.Permission;
import org.hive2hive.core.model.UserPermission;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>User Permission</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.impl.UserPermissionImpl#getUserid <em>Userid</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.UserPermissionImpl#getPermission <em>Permission</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class UserPermissionImpl extends MinimalEObjectImpl.Container implements UserPermission {
	/**
	 * The default value of the '{@link #getUserid() <em>Userid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUserid()
	 * @generated
	 * @ordered
	 */
	protected static final String USERID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUserid() <em>Userid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUserid()
	 * @generated
	 * @ordered
	 */
	protected String userid = USERID_EDEFAULT;

	/**
	 * The default value of the '{@link #getPermission() <em>Permission</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPermission()
	 * @generated
	 * @ordered
	 */
	protected static final Permission PERMISSION_EDEFAULT = Permission.READONLY;

	/**
	 * The cached value of the '{@link #getPermission() <em>Permission</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPermission()
	 * @generated
	 * @ordered
	 */
	protected Permission permission = PERMISSION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UserPermissionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.USER_PERMISSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getUserid() {
		return userid;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUserid(String newUserid) {
		String oldUserid = userid;
		userid = newUserid;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USER_PERMISSION__USERID, oldUserid, userid));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Permission getPermission() {
		return permission;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPermission(Permission newPermission) {
		Permission oldPermission = permission;
		permission = newPermission == null ? PERMISSION_EDEFAULT : newPermission;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USER_PERMISSION__PERMISSION, oldPermission, permission));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.USER_PERMISSION__USERID:
				return getUserid();
			case ModelPackage.USER_PERMISSION__PERMISSION:
				return getPermission();
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
			case ModelPackage.USER_PERMISSION__USERID:
				setUserid((String)newValue);
				return;
			case ModelPackage.USER_PERMISSION__PERMISSION:
				setPermission((Permission)newValue);
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
			case ModelPackage.USER_PERMISSION__USERID:
				setUserid(USERID_EDEFAULT);
				return;
			case ModelPackage.USER_PERMISSION__PERMISSION:
				setPermission(PERMISSION_EDEFAULT);
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
			case ModelPackage.USER_PERMISSION__USERID:
				return USERID_EDEFAULT == null ? userid != null : !USERID_EDEFAULT.equals(userid);
			case ModelPackage.USER_PERMISSION__PERMISSION:
				return permission != PERMISSION_EDEFAULT;
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
		result.append(" (userid: ");
		result.append(userid);
		result.append(", permission: ");
		result.append(permission);
		result.append(')');
		return result.toString();
	}

} //UserPermissionImpl
