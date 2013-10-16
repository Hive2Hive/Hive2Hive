/**
 */
package org.hive2hive.core.model.impl;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.hive2hive.core.model.MetaDocument;
import org.hive2hive.core.model.MetaFolder;
import org.hive2hive.core.model.ModelPackage;
import org.hive2hive.core.model.UserPermission;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Meta Folder</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.impl.MetaFolderImpl#getContent <em>Content</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.MetaFolderImpl#getUserPermission <em>User Permission</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MetaFolderImpl extends MetaDocumentImpl implements MetaFolder {
	/**
	 * The cached value of the '{@link #getContent() <em>Content</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContent()
	 * @generated
	 * @ordered
	 */
	protected EList<MetaDocument> content;

	/**
	 * The cached value of the '{@link #getUserPermission() <em>User Permission</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUserPermission()
	 * @generated
	 * @ordered
	 */
	protected EList<UserPermission> userPermission;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MetaFolderImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.META_FOLDER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MetaDocument> getContent() {
		if (content == null) {
			content = new EObjectResolvingEList<MetaDocument>(MetaDocument.class, this, ModelPackage.META_FOLDER__CONTENT);
		}
		return content;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<UserPermission> getUserPermission() {
		if (userPermission == null) {
			userPermission = new EObjectResolvingEList<UserPermission>(UserPermission.class, this, ModelPackage.META_FOLDER__USER_PERMISSION);
		}
		return userPermission;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.META_FOLDER__CONTENT:
				return getContent();
			case ModelPackage.META_FOLDER__USER_PERMISSION:
				return getUserPermission();
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
			case ModelPackage.META_FOLDER__CONTENT:
				getContent().clear();
				getContent().addAll((Collection<? extends MetaDocument>)newValue);
				return;
			case ModelPackage.META_FOLDER__USER_PERMISSION:
				getUserPermission().clear();
				getUserPermission().addAll((Collection<? extends UserPermission>)newValue);
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
			case ModelPackage.META_FOLDER__CONTENT:
				getContent().clear();
				return;
			case ModelPackage.META_FOLDER__USER_PERMISSION:
				getUserPermission().clear();
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
			case ModelPackage.META_FOLDER__CONTENT:
				return content != null && !content.isEmpty();
			case ModelPackage.META_FOLDER__USER_PERMISSION:
				return userPermission != null && !userPermission.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //MetaFolderImpl
