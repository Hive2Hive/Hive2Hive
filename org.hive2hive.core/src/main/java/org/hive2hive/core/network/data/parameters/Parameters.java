package org.hive2hive.core.network.data.parameters;

import java.security.KeyPair;
import java.security.PublicKey;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.NetworkContent;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.security.H2HDefaultEncryption;

/**
 * A parameter class for {@link DataManager}'s methods.
 * 
 * @author Seppi
 */
public class Parameters implements IParameters {

	private String locationKey;
	private String domainKey;
	private String contentKey;
	private Number160 lKey = H2HConstants.TOMP2P_DEFAULT_KEY;
	private Number160 dKey = H2HConstants.TOMP2P_DEFAULT_KEY;
	private Number160 cKey = H2HConstants.TOMP2P_DEFAULT_KEY;
	private Number160 vKey = H2HConstants.TOMP2P_DEFAULT_KEY;
	private NetworkContent data;
	private KeyPair protectionKeys;
	private KeyPair newProtectionKeys;
	private int ttl = -1;
	private boolean hashFlag = false;
	private byte[] hash;

	public Parameters setLocationKey(String locationKey) {
		this.locationKey = locationKey;
		this.lKey = Number160.createHash(locationKey);
		return this;
	}

	public Parameters setLocationKey(PublicKey key) {
		return setLocationKey(H2HDefaultEncryption.key2String(key));
	}

	@Override
	public String getLocationKey() {
		return locationKey;
	}

	@Override
	public Number160 getLKey() {
		return lKey;
	}

	public Parameters setDomainKey(String domainKey) {
		this.domainKey = domainKey;
		this.dKey = Number160.createHash(domainKey);
		return this;
	}

	@Override
	public String getDomainKey() {
		return domainKey;
	}

	@Override
	public Number160 getDKey() {
		return dKey;
	}

	public Parameters setContentKey(String contentKey) {
		this.contentKey = contentKey;
		this.cKey = Number160.createHash(contentKey);
		return this;
	}

	public Parameters setContentKey(Number160 contentKey) {
		this.cKey = contentKey;
		return this;
	}

	@Override
	public String getContentKey() {
		return contentKey;
	}

	@Override
	public Number160 getCKey() {
		return cKey;
	}

	public Parameters setVersionKey(Number160 versionKey) {
		this.vKey = versionKey;
		return this;
	}

	@Override
	public Number160 getVersionKey() {
		return vKey;
	}

	public Parameters setData(NetworkContent data) {
		this.data = data;
		return this;
	}

	@Override
	public NetworkContent getData() {
		return data;
	}

	public Parameters setProtectionKeys(KeyPair protectionKeys) {
		this.protectionKeys = protectionKeys;
		return this;
	}

	@Override
	public KeyPair getProtectionKeys() {
		return protectionKeys;
	}

	@Override
	public Number640 getKey() {
		return new Number640(lKey, dKey, cKey, vKey);
	}

	public Parameters setTTL(int ttl) {
		this.ttl = ttl;
		return this;
	}

	@Override
	public int getTTL() {
		return ttl;
	}

	public Parameters setNewProtectionKeys(KeyPair newProtectionKeys) {
		this.newProtectionKeys = newProtectionKeys;
		return this;
	}

	@Override
	public KeyPair getNewProtectionKeys() {
		return newProtectionKeys;
	}

	@Override
	public Parameters setHashFlag(boolean hashFlag) {
		this.hashFlag = hashFlag;
		return this;
	}

	@Override
	public boolean getHashFlag() {
		return hashFlag;
	}

	@Override
	public Parameters setHash(byte[] hash) {
		this.hash = hash;
		return this;
	}

	@Override
	public byte[] getHash() {
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (data != null) {
			builder.append("content = '").append(data.getClass().getSimpleName()).append("' ");
		}

		builder.append("location key = '").append(locationKey).append("' ");

		if (domainKey != null) {
			builder.append("domain key = '").append(domainKey).append("' ");
		}

		if (contentKey != null) {
			builder.append("content key = '").append(contentKey).append("' ");
		} else {
			builder.append("content key = '").append(cKey).append("' ");
		}

		if (!vKey.equals(H2HConstants.TOMP2P_DEFAULT_KEY)) {
			builder.append("version key = '").append(vKey).append("' ");
		}

		if (ttl != -1) {
			builder.append("ttl = '").append(ttl).append("' ");
		}

		if (protectionKeys != null) {
			builder.append("protected = 'true' ");
		}

		if (hashFlag) {
			builder.append("hashFlag = 'true'");
		}

		return builder.toString();
	}

}
