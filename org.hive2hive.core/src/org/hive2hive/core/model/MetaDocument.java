/**
 */
package org.hive2hive.core.model;

import java.security.PublicKey;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Meta Document</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.MetaDocument#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getMetaDocument()
 * @model abstract="true"
 * @generated
 */
public interface MetaDocument extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(PublicKey)
	 * @see org.hive2hive.core.model.ModelPackage#getMetaDocument_Id()
	 * @model dataType="org.hive2hive.core.model.PublicKey" required="true"
	 * @generated
	 */
	PublicKey getId();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.MetaDocument#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(PublicKey value);

} // MetaDocument
