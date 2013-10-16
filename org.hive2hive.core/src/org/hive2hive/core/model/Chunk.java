/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Chunk</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.Chunk#getData <em>Data</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getChunk()
 * @model
 * @generated
 */
public interface Chunk extends EObject {
	/**
	 * Returns the value of the '<em><b>Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data</em>' attribute.
	 * @see #setData(byte[])
	 * @see org.hive2hive.core.model.ModelPackage#getChunk_Data()
	 * @model required="true"
	 * @generated
	 */
	byte[] getData();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.Chunk#getData <em>Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data</em>' attribute.
	 * @see #getData()
	 * @generated
	 */
	void setData(byte[] value);

} // Chunk
