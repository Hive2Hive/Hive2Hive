package org.hive2hive.core.model;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the permissions for the folder (only one if private, but can have multiple users) and the keys for
 * the containing documents
 * 
 * @author Nico
 * 
 */
public class MetaFolder extends MetaDocument {

	private static final long serialVersionUID = 1L;
	private List<UserPermission> userPermissions;
	private List<KeyPair> childDocuments; // TODO really used??

	public MetaFolder(PublicKey id, String creatorName) {
		super(id);
		userPermissions = new ArrayList<UserPermission>();
		setChildDocuments(new ArrayList<KeyPair>());

		// creator receives write permissions by default
		userPermissions.add(new UserPermission(creatorName, PermissionType.WRITE));
	}

	public List<UserPermission> getUserPermissions() {
		return userPermissions;
	}

	public void setUserPermissions(List<UserPermission> userPermissions) {
		this.userPermissions = userPermissions;
	}

	public List<KeyPair> getChildDocuments() {
		return childDocuments;
	}

	public void setChildDocuments(List<KeyPair> childDocuments) {
		this.childDocuments = childDocuments;
	}

	public void addChildDocument(KeyPair child) {
		if (childDocuments == null) {
			childDocuments = new ArrayList<KeyPair>();
		}
		childDocuments.add(child);
	}

}
