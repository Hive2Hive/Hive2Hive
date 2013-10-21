/**
 */
package org.hive2hive.core.model.impl;

import java.security.KeyPair;
import java.security.PrivateKey;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.hive2hive.core.model.FileTree;
import org.hive2hive.core.model.FileTreeNode;
import org.hive2hive.core.model.ModelPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>File Tree Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.impl.FileTreeNodeImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.FileTreeNodeImpl#getKeyPair <em>Key Pair</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.FileTreeNodeImpl#getDomainKey <em>Domain Key</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.FileTreeNodeImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.FileTreeNodeImpl#isFolder <em>Folder</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FileTreeNodeImpl extends MinimalEObjectImpl.Container implements FileTreeNode {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

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
	 * The default value of the '{@link #getDomainKey() <em>Domain Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDomainKey()
	 * @generated
	 * @ordered
	 */
	protected static final PrivateKey DOMAIN_KEY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDomainKey() <em>Domain Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDomainKey()
	 * @generated
	 * @ordered
	 */
	protected PrivateKey domainKey = DOMAIN_KEY_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParent() <em>Parent</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParent()
	 * @generated
	 * @ordered
	 */
	protected FileTree parent;

	/**
	 * The default value of the '{@link #isFolder() <em>Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isFolder()
	 * @generated
	 * @ordered
	 */
	protected static final boolean FOLDER_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isFolder() <em>Folder</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isFolder()
	 * @generated
	 * @ordered
	 */
	protected boolean folder = FOLDER_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FileTreeNodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.FILE_TREE_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FILE_TREE_NODE__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FILE_TREE_NODE__KEY_PAIR, oldKeyPair, keyPair));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PrivateKey getDomainKey() {
		return domainKey;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDomainKey(PrivateKey newDomainKey) {
		PrivateKey oldDomainKey = domainKey;
		domainKey = newDomainKey;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FILE_TREE_NODE__DOMAIN_KEY, oldDomainKey, domainKey));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FileTree getParent() {
		if (parent != null && parent.eIsProxy()) {
			InternalEObject oldParent = (InternalEObject)parent;
			parent = (FileTree)eResolveProxy(oldParent);
			if (parent != oldParent) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.FILE_TREE_NODE__PARENT, oldParent, parent));
			}
		}
		return parent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FileTree basicGetParent() {
		return parent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(FileTree newParent) {
		FileTree oldParent = parent;
		parent = newParent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FILE_TREE_NODE__PARENT, oldParent, parent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isFolder() {
		return folder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFolder(boolean newFolder) {
		boolean oldFolder = folder;
		folder = newFolder;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FILE_TREE_NODE__FOLDER, oldFolder, folder));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isRoot() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isShared() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFullPath() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean canWrite() {
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
			case ModelPackage.FILE_TREE_NODE__NAME:
				return getName();
			case ModelPackage.FILE_TREE_NODE__KEY_PAIR:
				return getKeyPair();
			case ModelPackage.FILE_TREE_NODE__DOMAIN_KEY:
				return getDomainKey();
			case ModelPackage.FILE_TREE_NODE__PARENT:
				if (resolve) return getParent();
				return basicGetParent();
			case ModelPackage.FILE_TREE_NODE__FOLDER:
				return isFolder();
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
			case ModelPackage.FILE_TREE_NODE__NAME:
				setName((String)newValue);
				return;
			case ModelPackage.FILE_TREE_NODE__KEY_PAIR:
				setKeyPair((KeyPair)newValue);
				return;
			case ModelPackage.FILE_TREE_NODE__DOMAIN_KEY:
				setDomainKey((PrivateKey)newValue);
				return;
			case ModelPackage.FILE_TREE_NODE__PARENT:
				setParent((FileTree)newValue);
				return;
			case ModelPackage.FILE_TREE_NODE__FOLDER:
				setFolder((Boolean)newValue);
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
			case ModelPackage.FILE_TREE_NODE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ModelPackage.FILE_TREE_NODE__KEY_PAIR:
				setKeyPair(KEY_PAIR_EDEFAULT);
				return;
			case ModelPackage.FILE_TREE_NODE__DOMAIN_KEY:
				setDomainKey(DOMAIN_KEY_EDEFAULT);
				return;
			case ModelPackage.FILE_TREE_NODE__PARENT:
				setParent((FileTree)null);
				return;
			case ModelPackage.FILE_TREE_NODE__FOLDER:
				setFolder(FOLDER_EDEFAULT);
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
			case ModelPackage.FILE_TREE_NODE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ModelPackage.FILE_TREE_NODE__KEY_PAIR:
				return KEY_PAIR_EDEFAULT == null ? keyPair != null : !KEY_PAIR_EDEFAULT.equals(keyPair);
			case ModelPackage.FILE_TREE_NODE__DOMAIN_KEY:
				return DOMAIN_KEY_EDEFAULT == null ? domainKey != null : !DOMAIN_KEY_EDEFAULT.equals(domainKey);
			case ModelPackage.FILE_TREE_NODE__PARENT:
				return parent != null;
			case ModelPackage.FILE_TREE_NODE__FOLDER:
				return folder != FOLDER_EDEFAULT;
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
		result.append(" (name: ");
		result.append(name);
		result.append(", keyPair: ");
		result.append(keyPair);
		result.append(", domainKey: ");
		result.append(domainKey);
		result.append(", folder: ");
		result.append(folder);
		result.append(')');
		return result.toString();
	}

} //FileTreeNodeImpl
