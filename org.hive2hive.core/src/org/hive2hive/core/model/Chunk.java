/**
 */
package org.hive2hive.core.model;

import java.security.PublicKey;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Chunk</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.Chunk#getId <em>Id</em>}</li>
 *   <li>{@link org.hive2hive.core.model.Chunk#getData <em>Data</em>}</li>
 *   <li>{@link org.hive2hive.core.model.Chunk#getOrder <em>Order</em>}</li>
 *   <li>{@link org.hive2hive.core.model.Chunk#getSize <em>Size</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getChunk()
 * @model
 * @generated
 */
public interface Chunk extends EObject {
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
	 * @see org.hive2hive.core.model.ModelPackage#getChunk_Id()
	 * @model dataType="org.hive2hive.core.model.PublicKey" required="true"
	 * @generated
	 */
	PublicKey getId();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.Chunk#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(PublicKey value);

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

	/**
	 * Returns the value of the '<em><b>Order</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Order</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Order</em>' attribute.
	 * @see #setOrder(int)
	 * @see org.hive2hive.core.model.ModelPackage#getChunk_Order()
	 * @model required="true"
	 * @generated
	 */
	int getOrder();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.Chunk#getOrder <em>Order</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Order</em>' attribute.
	 * @see #getOrder()
	 * @generated
	 */
	void setOrder(int value);

	/**
	 * Returns the value of the '<em><b>Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Size</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Size</em>' attribute.
	 * @see #setSize(long)
	 * @see org.hive2hive.core.model.ModelPackage#getChunk_Size()
	 * @model required="true"
	 * @generated
	 */
	long getSize();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.Chunk#getSize <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Size</em>' attribute.
	 * @see #getSize()
	 * @generated
	 */
	void setSize(long value);

} // Chunk
