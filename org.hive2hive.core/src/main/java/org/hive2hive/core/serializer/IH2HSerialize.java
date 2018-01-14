package org.hive2hive.core.serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * Provides serialization and deserialization of objects. Note that different
 * nodes cannot have different serialization implementations.
 * 
 * @author Nico
 */
public interface IH2HSerialize {

	/**
	 * Serializes an object to a byte array which can be sent over the network
	 * 
	 * @param object
	 *            the object to serialize
	 * @return the serialized object data
	 * @throws IOException in case the object cannot be serialized
	 */
	byte[] serialize(Serializable object) throws IOException;

	/**
	 * Deserializes an object from a byte array. The object type is not yet specific
	 * 
	 * @param bytes
	 *            the object data to deserialize
	 * @return the deserialized object
	 * @throws IOException in case the object cannot be deserialized
	 * @throws ClassNotFoundException in case the object's class cannot be found
	 */
	Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException;
}
