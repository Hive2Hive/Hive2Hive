package org.hive2hive.core.api.interfaces;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.file.IFileAgent;
import org.hive2hive.core.security.UserCredentials;
import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Basic interface for all user management operations.
 * 
 * @author Christian, Nico, Seppi
 * 
 */
public interface IUserManager {

	/**
	 * Each user must be registered in the network. This call creates a new UserProfile for the
	 * given user credentials.
	 * 
	 * @param credentials the user credentials. Note that the user id must be unique, the password and pin
	 *            must be kept private to ensure the security.
	 * @return A registration process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 */
	IProcessComponent<Void> createRegisterProcess(UserCredentials credentials) throws NoPeerConnectionException;

	/**
	 * Login a (registered) user with the same credentials as
	 * {@link IUserManager#createRegisterProcess(UserCredentials)} has been called. After login, the root
	 * folder gets synchronized.
	 * 
	 * @param credentials the user credentials
	 * @param fileAgent handles needed file operations and provides the root folder of this user
	 * @return A login process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 */
	IProcessComponent<Void> createLoginProcess(UserCredentials credentials, IFileAgent fileAgent)
			throws NoPeerConnectionException;

	/**
	 * When a user is done, he should logout himself, killing the session at the current node. After logout,
	 * he does not receive any messages / notifications anymore and files don't get synchronized anymore.
	 * 
	 * @return A logout process.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 * @throws NoSessionException If not user has logged in.
	 */
	IProcessComponent<Void> createLogoutProcess() throws NoPeerConnectionException, NoSessionException;

	/**
	 * Checks whether a user is registered in the network.
	 * 
	 * @param userId The ID of the user.
	 * @return <code>True</code> if logged in, <code>false</code> otherwise.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 */
	boolean isRegistered(String userId) throws NoPeerConnectionException;

	/**
	 * Checks whether the user is logged in on the network.
	 * 
	 * @return <code>True</code> if logged in, <code>false</code> otherwise.
	 * @throws NoPeerConnectionException If the peer is not connected to the network.
	 */
	boolean isLoggedIn() throws NoPeerConnectionException;
}
