package org.hive2hive.core.utils;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Utility queue that can be serialized.</br>
 * <b>Note:</b> Only insert objects that implement {@link Serializable}.
 * @author Christian
 *
 * @param <T>
 */
public class SerializableLinkedList<T extends Serializable> extends LinkedList<Serializable> implements Serializable {

	private static final long serialVersionUID = 8430500773679362888L;
}
