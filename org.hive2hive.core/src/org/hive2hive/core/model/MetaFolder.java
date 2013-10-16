/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Meta Folder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.MetaFolder#getContent <em>Content</em>}</li>
 *   <li>{@link org.hive2hive.core.model.MetaFolder#getUserPermission <em>User Permission</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getMetaFolder()
 * @model
 * @generated
 */
public interface MetaFolder extends MetaDocument {
	/**
	 * Returns the value of the '<em><b>Content</b></em>' reference list.
	 * The list contents are of type {@link org.hive2hive.core.model.MetaDocument}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Content</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Content</em>' reference list.
	 * @see org.hive2hive.core.model.ModelPackage#getMetaFolder_Content()
	 * @model
	 * @generated
	 */
	EList<MetaDocument> getContent();

	/**
	 * Returns the value of the '<em><b>User Permission</b></em>' reference list.
	 * The list contents are of type {@link org.hive2hive.core.model.UserPermission}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>User Permission</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User Permission</em>' reference list.
	 * @see org.hive2hive.core.model.ModelPackage#getMetaFolder_UserPermission()
	 * @model
	 * @generated
	 */
	EList<UserPermission> getUserPermission();

} // MetaFolder
