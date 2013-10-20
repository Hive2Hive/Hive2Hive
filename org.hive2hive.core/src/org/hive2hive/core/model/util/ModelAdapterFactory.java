/**
 */
package org.hive2hive.core.model.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.hive2hive.core.model.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.hive2hive.core.model.ModelPackage
 * @generated
 */
public class ModelAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ModelPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = ModelPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelSwitch<Adapter> modelSwitch =
		new ModelSwitch<Adapter>() {
			@Override
			public Adapter caseMetaFile(MetaFile object) {
				return createMetaFileAdapter();
			}
			@Override
			public Adapter caseVersion(Version object) {
				return createVersionAdapter();
			}
			@Override
			public Adapter caseUserPermission(UserPermission object) {
				return createUserPermissionAdapter();
			}
			@Override
			public Adapter caseMetaDocument(MetaDocument object) {
				return createMetaDocumentAdapter();
			}
			@Override
			public Adapter caseMetaFolder(MetaFolder object) {
				return createMetaFolderAdapter();
			}
			@Override
			public Adapter caseChunk(Chunk object) {
				return createChunkAdapter();
			}
			@Override
			public Adapter caseUserProfile(UserProfile object) {
				return createUserProfileAdapter();
			}
			@Override
			public Adapter caseFileTree(FileTree object) {
				return createFileTreeAdapter();
			}
			@Override
			public Adapter caseFileTreeNode(FileTreeNode object) {
				return createFileTreeNodeAdapter();
			}
			@Override
			public Adapter caseOnlinePeer(OnlinePeer object) {
				return createOnlinePeerAdapter();
			}
			@Override
			public Adapter caseLocations(Locations object) {
				return createLocationsAdapter();
			}
			@Override
			public Adapter caseUserMessage(UserMessage object) {
				return createUserMessageAdapter();
			}
			@Override
			public Adapter caseUserMessageQueue(UserMessageQueue object) {
				return createUserMessageQueueAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.MetaFile <em>Meta File</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.MetaFile
	 * @generated
	 */
	public Adapter createMetaFileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.Version <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.Version
	 * @generated
	 */
	public Adapter createVersionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.UserPermission <em>User Permission</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.UserPermission
	 * @generated
	 */
	public Adapter createUserPermissionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.MetaDocument <em>Meta Document</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.MetaDocument
	 * @generated
	 */
	public Adapter createMetaDocumentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.MetaFolder <em>Meta Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.MetaFolder
	 * @generated
	 */
	public Adapter createMetaFolderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.Chunk <em>Chunk</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.Chunk
	 * @generated
	 */
	public Adapter createChunkAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.UserProfile <em>User Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.UserProfile
	 * @generated
	 */
	public Adapter createUserProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.FileTree <em>File Tree</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.FileTree
	 * @generated
	 */
	public Adapter createFileTreeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.FileTreeNode <em>File Tree Node</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.FileTreeNode
	 * @generated
	 */
	public Adapter createFileTreeNodeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.OnlinePeer <em>Online Peer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.OnlinePeer
	 * @generated
	 */
	public Adapter createOnlinePeerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.Locations <em>Locations</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.Locations
	 * @generated
	 */
	public Adapter createLocationsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.UserMessage <em>User Message</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.UserMessage
	 * @generated
	 */
	public Adapter createUserMessageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.hive2hive.core.model.UserMessageQueue <em>User Message Queue</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.hive2hive.core.model.UserMessageQueue
	 * @generated
	 */
	public Adapter createUserMessageQueueAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //ModelAdapterFactory
