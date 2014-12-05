package org.hive2hive.core.security;

import java.io.IOException;
import java.io.Serializable;

/**
 * Provides serialization and deserialization of objects.
 * Note that different nodes cannot have different serialization implementations.
 * 
 * @author Nico
 */
public interface IH2HSerialize {

	/**
	 * Serializes an object to a byte array which can be sent over the network
	 */
	byte[] serialize(Serializable object) throws IOException;

	/**
	 * Deserializes an object from a byte array. The object type is not yet specific
	 */
	Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException;

}
