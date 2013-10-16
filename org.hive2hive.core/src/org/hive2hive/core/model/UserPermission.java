/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>User Permission</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.UserPermission#getUserid <em>Userid</em>}</li>
 *   <li>{@link org.hive2hive.core.model.UserPermission#getPermission <em>Permission</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getUserPermission()
 * @model
 * @generated
 */
public interface UserPermission extends EObject {
	/**
	 * Returns the value of the '<em><b>Userid</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Userid</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Userid</em>' attribute.
	 * @see #setUserid(String)
	 * @see org.hive2hive.core.model.ModelPackage#getUserPermission_Userid()
	 * @model required="true"
	 * @generated
	 */
	String getUserid();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserPermission#getUserid <em>Userid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Userid</em>' attribute.
	 * @see #getUserid()
	 * @generated
	 */
	void setUserid(String value);

	/**
	 * Returns the value of the '<em><b>Permission</b></em>' attribute.
	 * The default value is <code>"permission"</code>.
	 * The literals are from the enumeration {@link org.hive2hive.core.model.Permission}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Permission</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Permission</em>' attribute.
	 * @see org.hive2hive.core.model.Permission
	 * @see #setPermission(Permission)
	 * @see org.hive2hive.core.model.ModelPackage#getUserPermission_Permission()
	 * @model default="permission" required="true"
	 * @generated
	 */
	Permission getPermission();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.UserPermission#getPermission <em>Permission</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Permission</em>' attribute.
	 * @see org.hive2hive.core.model.Permission
	 * @see #getPermission()
	 * @generated
	 */
	void setPermission(Permission value);

} // UserPermission
