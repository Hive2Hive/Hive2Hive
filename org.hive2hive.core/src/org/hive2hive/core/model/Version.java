/**
 */
package org.hive2hive.core.model;

import java.security.KeyPair;
import java.util.Date;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Version</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.Version#getChunks <em>Chunks</em>}</li>
 *   <li>{@link org.hive2hive.core.model.Version#getCounter <em>Counter</em>}</li>
 *   <li>{@link org.hive2hive.core.model.Version#getSize <em>Size</em>}</li>
 *   <li>{@link org.hive2hive.core.model.Version#getDate <em>Date</em>}</li>
 *   <li>{@link org.hive2hive.core.model.Version#getChunkKeys <em>Chunk Keys</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getVersion()
 * @model
 * @generated
 */
public interface Version extends EObject {

	/**
	 * Returns the value of the '<em><b>Chunks</b></em>' reference list.
	 * The list contents are of type {@link org.hive2hive.core.model.Chunk}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Chunks</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Chunks</em>' reference list.
	 * @see org.hive2hive.core.model.ModelPackage#getVersion_Chunks()
	 * @model
	 * @generated
	 */
	EList<Chunk> getChunks();

	/**
	 * Returns the value of the '<em><b>Counter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Counter</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Counter</em>' attribute.
	 * @see #setCounter(int)
	 * @see org.hive2hive.core.model.ModelPackage#getVersion_Counter()
	 * @model required="true"
	 * @generated
	 */
	int getCounter();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.Version#getCounter <em>Counter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Counter</em>' attribute.
	 * @see #getCounter()
	 * @generated
	 */
	void setCounter(int value);

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
	 * @see org.hive2hive.core.model.ModelPackage#getVersion_Size()
	 * @model required="true"
	 * @generated
	 */
	long getSize();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.Version#getSize <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Size</em>' attribute.
	 * @see #getSize()
	 * @generated
	 */
	void setSize(long value);

	/**
	 * Returns the value of the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date</em>' attribute.
	 * @see #setDate(Date)
	 * @see org.hive2hive.core.model.ModelPackage#getVersion_Date()
	 * @model required="true"
	 * @generated
	 */
	Date getDate();

	/**
	 * Sets the value of the '{@link org.hive2hive.core.model.Version#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(Date value);

	/**
	 * Returns the value of the '<em><b>Chunk Keys</b></em>' attribute list.
	 * The list contents are of type {@link java.security.KeyPair}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Chunk Keys</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Chunk Keys</em>' attribute list.
	 * @see org.hive2hive.core.model.ModelPackage#getVersion_ChunkKeys()
	 * @model dataType="org.hive2hive.core.model.KeyPair"
	 * @generated
	 */
	EList<KeyPair> getChunkKeys();
} // Version
