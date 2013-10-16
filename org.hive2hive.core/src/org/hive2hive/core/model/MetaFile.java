/**
 */
package org.hive2hive.core.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Meta File</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.MetaFile#getVersions <em>Versions</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.hive2hive.core.model.ModelPackage#getMetaFile()
 * @model
 * @generated
 */
public interface MetaFile extends MetaDocument {
	/**
	 * Returns the value of the '<em><b>Versions</b></em>' reference list.
	 * The list contents are of type {@link org.hive2hive.core.model.Version}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Versions</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Versions</em>' reference list.
	 * @see org.hive2hive.core.model.ModelPackage#getMetaFile_Versions()
	 * @model
	 * @generated
	 */
	EList<Version> getVersions();

} // MetaFile
