/**
 */
package org.hive2hive.core.model.impl;

import java.security.KeyPair;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.hive2hive.core.model.FileTree;
import org.hive2hive.core.model.ModelPackage;
import org.hive2hive.core.model.UserProfile;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>User Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.impl.UserProfileImpl#getFileTree <em>File Tree</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.UserProfileImpl#getEncryptionKeys <em>Encryption Keys</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.UserProfileImpl#getSignatureKeys <em>Signature Keys</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.UserProfileImpl#getUserId <em>User Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class UserProfileImpl extends MinimalEObjectImpl.Container implements UserProfile {
	/**
	 * The cached value of the '{@link #getFileTree() <em>File Tree</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileTree()
	 * @generated
	 * @ordered
	 */
	protected FileTree fileTree;

	/**
	 * The default value of the '{@link #getEncryptionKeys() <em>Encryption Keys</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEncryptionKeys()
	 * @generated
	 * @ordered
	 */
	protected static final KeyPair ENCRYPTION_KEYS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEncryptionKeys() <em>Encryption Keys</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEncryptionKeys()
	 * @generated
	 * @ordered
	 */
	protected KeyPair encryptionKeys = ENCRYPTION_KEYS_EDEFAULT;

	/**
	 * The default value of the '{@link #getSignatureKeys() <em>Signature Keys</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSignatureKeys()
	 * @generated
	 * @ordered
	 */
	protected static final KeyPair SIGNATURE_KEYS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSignatureKeys() <em>Signature Keys</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSignatureKeys()
	 * @generated
	 * @ordered
	 */
	protected KeyPair signatureKeys = SIGNATURE_KEYS_EDEFAULT;

	/**
	 * The default value of the '{@link #getUserId() <em>User Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUserId()
	 * @generated
	 * @ordered
	 */
	protected static final String USER_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUserId() <em>User Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUserId()
	 * @generated
	 * @ordered
	 */
	protected String userId = USER_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UserProfileImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.USER_PROFILE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FileTree getFileTree() {
		if (fileTree != null && fileTree.eIsProxy()) {
			InternalEObject oldFileTree = (InternalEObject)fileTree;
			fileTree = (FileTree)eResolveProxy(oldFileTree);
			if (fileTree != oldFileTree) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.USER_PROFILE__FILE_TREE, oldFileTree, fileTree));
			}
		}
		return fileTree;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FileTree basicGetFileTree() {
		return fileTree;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFileTree(FileTree newFileTree) {
		FileTree oldFileTree = fileTree;
		fileTree = newFileTree;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USER_PROFILE__FILE_TREE, oldFileTree, fileTree));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public KeyPair getEncryptionKeys() {
		return encryptionKeys;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEncryptionKeys(KeyPair newEncryptionKeys) {
		KeyPair oldEncryptionKeys = encryptionKeys;
		encryptionKeys = newEncryptionKeys;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USER_PROFILE__ENCRYPTION_KEYS, oldEncryptionKeys, encryptionKeys));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public KeyPair getSignatureKeys() {
		return signatureKeys;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSignatureKeys(KeyPair newSignatureKeys) {
		KeyPair oldSignatureKeys = signatureKeys;
		signatureKeys = newSignatureKeys;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USER_PROFILE__SIGNATURE_KEYS, oldSignatureKeys, signatureKeys));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUserId(String newUserId) {
		String oldUserId = userId;
		userId = newUserId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.USER_PROFILE__USER_ID, oldUserId, userId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.USER_PROFILE__FILE_TREE:
				if (resolve) return getFileTree();
				return basicGetFileTree();
			case ModelPackage.USER_PROFILE__ENCRYPTION_KEYS:
				return getEncryptionKeys();
			case ModelPackage.USER_PROFILE__SIGNATURE_KEYS:
				return getSignatureKeys();
			case ModelPackage.USER_PROFILE__USER_ID:
				return getUserId();
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
			case ModelPackage.USER_PROFILE__FILE_TREE:
				setFileTree((FileTree)newValue);
				return;
			case ModelPackage.USER_PROFILE__ENCRYPTION_KEYS:
				setEncryptionKeys((KeyPair)newValue);
				return;
			case ModelPackage.USER_PROFILE__SIGNATURE_KEYS:
				setSignatureKeys((KeyPair)newValue);
				return;
			case ModelPackage.USER_PROFILE__USER_ID:
				setUserId((String)newValue);
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
			case ModelPackage.USER_PROFILE__FILE_TREE:
				setFileTree((FileTree)null);
				return;
			case ModelPackage.USER_PROFILE__ENCRYPTION_KEYS:
				setEncryptionKeys(ENCRYPTION_KEYS_EDEFAULT);
				return;
			case ModelPackage.USER_PROFILE__SIGNATURE_KEYS:
				setSignatureKeys(SIGNATURE_KEYS_EDEFAULT);
				return;
			case ModelPackage.USER_PROFILE__USER_ID:
				setUserId(USER_ID_EDEFAULT);
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
			case ModelPackage.USER_PROFILE__FILE_TREE:
				return fileTree != null;
			case ModelPackage.USER_PROFILE__ENCRYPTION_KEYS:
				return ENCRYPTION_KEYS_EDEFAULT == null ? encryptionKeys != null : !ENCRYPTION_KEYS_EDEFAULT.equals(encryptionKeys);
			case ModelPackage.USER_PROFILE__SIGNATURE_KEYS:
				return SIGNATURE_KEYS_EDEFAULT == null ? signatureKeys != null : !SIGNATURE_KEYS_EDEFAULT.equals(signatureKeys);
			case ModelPackage.USER_PROFILE__USER_ID:
				return USER_ID_EDEFAULT == null ? userId != null : !USER_ID_EDEFAULT.equals(userId);
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
		result.append(" (encryptionKeys: ");
		result.append(encryptionKeys);
		result.append(", signatureKeys: ");
		result.append(signatureKeys);
		result.append(", userId: ");
		result.append(userId);
		result.append(')');
		return result.toString();
	}

} //UserProfileImpl
