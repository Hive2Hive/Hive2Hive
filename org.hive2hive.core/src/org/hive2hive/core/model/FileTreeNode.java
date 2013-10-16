/**
 */
package org.hive2hive.core.model;

import java.security.KeyPair;
import java.security.PrivateKey;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>File Tree Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.FileTreeNode#getKeyPair <em>Key Pair</em>}</li>
 *   <li>{@link org.hive2hive.core.model.FileTreeNode#getDomainKey <em>Domain Key</em>}</li>
 *   <li>{@link org.hive2hive.core.model.FileTreeNode#getParent <em>Parent</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getFileTreeNode()
 * @model
 * @generated
 */
public interface FileTreeNode extends EObject {
	/**
	 * Returns the value of the '<em><b>Key Pair</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Key Pair</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Key Pair</em>' attribute.
	 * @see #setKeyPair(KeyPair)
	 * @see org.hive2hive.core.model.ModelPackage#getFileTreeNode_KeyPair()
	 * @model dataType="org.hive2hive.core.model.KeyPair" required="true"
	 * @generated
	 */
	KeyPair getKeyPair();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.FileTreeNode#getKeyPair <em>Key Pair</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Key Pair</em>' attribute.
	 * @see #getKeyPair()
	 * @generated
	 */
	void setKeyPair(KeyPair value);

	/**
	 * Returns the value of the '<em><b>Domain Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Domain Key</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Domain Key</em>' attribute.
	 * @see #setDomainKey(PrivateKey)
	 * @see org.hive2hive.core.model.ModelPackage#getFileTreeNode_DomainKey()
	 * @model dataType="org.hive2hive.core.model.PrivateKey"
	 * @generated
	 */
	PrivateKey getDomainKey();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.FileTreeNode#getDomainKey <em>Domain Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Domain Key</em>' attribute.
	 * @see #getDomainKey()
	 * @generated
	 */
	void setDomainKey(PrivateKey value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' reference.
	 * @see #setParent(FileTree)
	 * @see org.hive2hive.core.model.ModelPackage#getFileTreeNode_Parent()
	 * @model
	 * @generated
	 */
	FileTree getParent();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.FileTreeNode#getParent <em>Parent</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(FileTree value);

} // FileTreeNode
