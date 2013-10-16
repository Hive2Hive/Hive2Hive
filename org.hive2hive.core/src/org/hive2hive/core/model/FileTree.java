/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>File Tree</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.FileTree#getChildren <em>Children</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getFileTree()
 * @model
 * @generated
 */
public interface FileTree extends EObject {
	/**
	 * Returns the value of the '<em><b>Children</b></em>' reference list.
	 * The list contents are of type {@link org.hive2hive.core.model.FileTreeNode}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' reference list.
	 * @see org.hive2hive.core.model.ModelPackage#getFileTree_Children()
	 * @model
	 * @generated
	 */
	EList<FileTreeNode> getChildren();

} // FileTree
