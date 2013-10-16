/**
 */
package org.hive2hive.core.model.impl;

import java.security.KeyPair;
import java.util.Collection;
import java.util.Date;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.hive2hive.core.model.Chunk;
import org.hive2hive.core.model.ModelPackage;
import org.hive2hive.core.model.Version;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Version</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.hive2hive.core.model.impl.VersionImpl#getChunks <em>Chunks</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.VersionImpl#getCounter <em>Counter</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.VersionImpl#getSize <em>Size</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.VersionImpl#getDate <em>Date</em>}</li>
 *   <li>{@link org.hive2hive.core.model.impl.VersionImpl#getChunkKeys <em>Chunk Keys</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class VersionImpl extends MinimalEObjectImpl.Container implements Version {
	/**
	 * The cached value of the '{@link #getChunks() <em>Chunks</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChunks()
	 * @generated
	 * @ordered
	 */
	protected EList<Chunk> chunks;
	/**
	 * The default value of the '{@link #getCounter() <em>Counter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounter()
	 * @generated
	 * @ordered
	 */
	protected static final int COUNTER_EDEFAULT = 0;
	/**
	 * The cached value of the '{@link #getCounter() <em>Counter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCounter()
	 * @generated
	 * @ordered
	 */
	protected int counter = COUNTER_EDEFAULT;
	/**
	 * The default value of the '{@link #getSize() <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSize()
	 * @generated
	 * @ordered
	 */
	protected static final long SIZE_EDEFAULT = 0L;
	/**
	 * The cached value of the '{@link #getSize() <em>Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSize()
	 * @generated
	 * @ordered
	 */
	protected long size = SIZE_EDEFAULT;
	/**
	 * The default value of the '{@link #getDate() <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDate()
	 * @generated
	 * @ordered
	 */
	protected static final Date DATE_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getDate() <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDate()
	 * @generated
	 * @ordered
	 */
	protected Date date = DATE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChunkKeys() <em>Chunk Keys</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChunkKeys()
	 * @generated
	 * @ordered
	 */
	protected EList<KeyPair> chunkKeys;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VersionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.VERSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Chunk> getChunks() {
		if (chunks == null) {
			chunks = new EObjectResolvingEList<Chunk>(Chunk.class, this, ModelPackage.VERSION__CHUNKS);
		}
		return chunks;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCounter(int newCounter) {
		int oldCounter = counter;
		counter = newCounter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.VERSION__COUNTER, oldCounter, counter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public long getSize() {
		return size;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSize(long newSize) {
		long oldSize = size;
		size = newSize;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.VERSION__SIZE, oldSize, size));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDate(Date newDate) {
		Date oldDate = date;
		date = newDate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.VERSION__DATE, oldDate, date));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<KeyPair> getChunkKeys() {
		if (chunkKeys == null) {
			chunkKeys = new EDataTypeUniqueEList<KeyPair>(KeyPair.class, this, ModelPackage.VERSION__CHUNK_KEYS);
		}
		return chunkKeys;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelPackage.VERSION__CHUNKS:
				return getChunks();
			case ModelPackage.VERSION__COUNTER:
				return getCounter();
			case ModelPackage.VERSION__SIZE:
				return getSize();
			case ModelPackage.VERSION__DATE:
				return getDate();
			case ModelPackage.VERSION__CHUNK_KEYS:
				return getChunkKeys();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelPackage.VERSION__CHUNKS:
				getChunks().clear();
				getChunks().addAll((Collection<? extends Chunk>)newValue);
				return;
			case ModelPackage.VERSION__COUNTER:
				setCounter((Integer)newValue);
				return;
			case ModelPackage.VERSION__SIZE:
				setSize((Long)newValue);
				return;
			case ModelPackage.VERSION__DATE:
				setDate((Date)newValue);
				return;
			case ModelPackage.VERSION__CHUNK_KEYS:
				getChunkKeys().clear();
				getChunkKeys().addAll((Collection<? extends KeyPair>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ModelPackage.VERSION__CHUNKS:
				getChunks().clear();
				return;
			case ModelPackage.VERSION__COUNTER:
				setCounter(COUNTER_EDEFAULT);
				return;
			case ModelPackage.VERSION__SIZE:
				setSize(SIZE_EDEFAULT);
				return;
			case ModelPackage.VERSION__DATE:
				setDate(DATE_EDEFAULT);
				return;
			case ModelPackage.VERSION__CHUNK_KEYS:
				getChunkKeys().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ModelPackage.VERSION__CHUNKS:
				return chunks != null && !chunks.isEmpty();
			case ModelPackage.VERSION__COUNTER:
				return counter != COUNTER_EDEFAULT;
			case ModelPackage.VERSION__SIZE:
				return size != SIZE_EDEFAULT;
			case ModelPackage.VERSION__DATE:
				return DATE_EDEFAULT == null ? date != null : !DATE_EDEFAULT.equals(date);
			case ModelPackage.VERSION__CHUNK_KEYS:
				return chunkKeys != null && !chunkKeys.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (counter: ");
		result.append(counter);
		result.append(", size: ");
		result.append(size);
		result.append(", date: ");
		result.append(date);
		result.append(", chunkKeys: ");
		result.append(chunkKeys);
		result.append(')');
		return result.toString();
	}

} //VersionImpl
