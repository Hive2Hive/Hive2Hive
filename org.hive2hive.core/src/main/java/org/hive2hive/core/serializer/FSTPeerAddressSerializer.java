package org.hive2hive.core.serializer;

import java.io.IOException;

import net.tomp2p.peers.PeerAddress;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTClazzInfo.FSTFieldInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

@SuppressWarnings("rawtypes")
public class FSTPeerAddressSerializer extends FSTBasicObjectSerializer {

	@Override
	public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTFieldInfo referencedBy,
			int streamPosition) throws IOException {
		byte[] value = ((PeerAddress) toWrite).toByteArray();
		out.writeInt(value.length);
		out.write(value);
	}

	@Override
	public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTFieldInfo referencee,
			int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		byte[] buf = new byte[in.readInt()];
		in.read(buf);
		PeerAddress address = new PeerAddress(buf);
		in.registerObject(address, streamPosition, serializationInfo, referencee);
		return address;
	}

}
