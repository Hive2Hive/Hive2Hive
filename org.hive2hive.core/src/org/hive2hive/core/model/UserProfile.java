/**
 */
package org.hive2hive.core.model;

import java.security.KeyPair;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>User Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.UserProfile#getFileTree <em>File Tree</em>}</li>
 *   <li>{@link org.hive2hive.core.model.UserProfile#getEncryptionKeys <em>Encryption Keys</em>}</li>
 *   <li>{@link org.hive2hive.core.model.UserProfile#getSignatureKeys <em>Signature Keys</em>}</li>
 *   <li>{@link org.hive2hive.core.model.UserProfile#getUserId <em>User Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getUserProfile()
 * @model
 * @generated
 */
public interface UserProfile extends EObject {
	/**
	 * Returns the value of the '<em><b>File Tree</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>File Tree</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Tree</em>' reference.
	 * @see #setFileTree(FileTree)
	 * @see org.hive2hive.core.model.ModelPackage#getUserProfile_FileTree()
	 * @model required="true"
	 * @generated
	 */
	FileTree getFileTree();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserProfile#getFileTree <em>File Tree</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Tree</em>' reference.
	 * @see #getFileTree()
	 * @generated
	 */
	void setFileTree(FileTree value);

	/**
	 * Returns the value of the '<em><b>Encryption Keys</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Encryption Keys</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Encryption Keys</em>' attribute.
	 * @see #setEncryptionKeys(KeyPair)
	 * @see org.hive2hive.core.model.ModelPackage#getUserProfile_EncryptionKeys()
	 * @model dataType="org.hive2hive.core.model.KeyPair" required="true"
	 * @generated
	 */
	KeyPair getEncryptionKeys();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserProfile#getEncryptionKeys <em>Encryption Keys</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Encryption Keys</em>' attribute.
	 * @see #getEncryptionKeys()
	 * @generated
	 */
	void setEncryptionKeys(KeyPair value);

	/**
	 * Returns the value of the '<em><b>Signature Keys</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Signature Keys</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Signature Keys</em>' attribute.
	 * @see #setSignatureKeys(KeyPair)
	 * @see org.hive2hive.core.model.ModelPackage#getUserProfile_SignatureKeys()
	 * @model dataType="org.hive2hive.core.model.KeyPair" required="true"
	 * @generated
	 */
	KeyPair getSignatureKeys();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserProfile#getSignatureKeys <em>Signature Keys</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Signature Keys</em>' attribute.
	 * @see #getSignatureKeys()
	 * @generated
	 */
	void setSignatureKeys(KeyPair value);

	/**
	 * Returns the value of the '<em><b>User Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>User Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User Id</em>' attribute.
	 * @see #setUserId(String)
	 * @see org.hive2hive.core.model.ModelPackage#getUserProfile_UserId()
	 * @model required="true"
	 * @generated
	 */
	String getUserId();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserProfile#getUserId <em>User Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>User Id</em>' attribute.
	 * @see #getUserId()
	 * @generated
	 */
	void setUserId(String value);

} // UserProfile
