package org.hive2hive.core.peerpersistencemodel.util;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.model.ModelPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.peerpersistencemodel.PeerMemory;
import org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelFactory;
import org.hive2hive.core.peerpersistencemodel.PeerPersistenceModelPackage;

/**
 * Convenience utile class to ease the use of the peer persistence.
 * 
 * @author Nendor
 * 
 */
public class PeerPersistenceModelUtil {

	private static final H2HLogger logger = H2HLoggerFactory
			.getLogger(PeerPersistenceModelUtil.class);

	private static final ResourceSet resourceSet;

	// Initializing the resource set.
	static {
		resourceSet = new ResourceSetImpl();
		resourceSet
				.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(ModelPackage.eNS_URI,
				ModelPackage.eINSTANCE);
	}
	
	private static PeerMemory peerMemory;

	public static void savePeerMemory() {
		initialize();
		Resource resource = getPeerResource();
		resource.getContents().add(peerMemory);
		try {
			resource.save(Collections.EMPTY_MAP);
			logger.debug("Peer memory saved successfully.");
		} catch (Exception e) {
			logger.error(String.format(
					"Exception while saving the peer memory: '%s'",
					e.getMessage()));
		}
	}

	public static void loadPeerMemory() {
		File saveFile = new File(H2HConstants.PEER_PERSISTENCE_FILE_NAME);
		if (!saveFile.exists()) {
			createInitialPeerMemory();
		} else {
			initialize();
			Resource resource = getPeerResource();
			try {
				resource.load(Collections.EMPTY_MAP);
			} catch (Exception e) {
				logger.error(String.format(
						"Exception while loading the peer memory: '%s'",
						e.getMessage()));
				peerMemory = null;
				return;
			}
			peerMemory = (PeerMemory) resource.getContents().get(0);
			logger.debug("Peer memory loaded successfully.");
		}
	}

	private static void initialize() {
		// Initialize the model
		PeerPersistenceModelPackage.eINSTANCE.eClass();

		// Register the XMI resource factory for the .peerpersistencemodel
		// extension
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("peerpersistencemodel", new XMIResourceFactoryImpl());
	}

	private static Resource getPeerResource() {
		File saveFile = new File(H2HConstants.PEER_PERSISTENCE_FILE_NAME);
		URI fileURI = URI.createFileURI(saveFile.getAbsolutePath());
		Resource resource = resourceSet.createResource(fileURI);
		return resource;
	}

	private static void createInitialPeerMemory() {
		peerMemory = PeerPersistenceModelFactory.eINSTANCE
				.createPeerMemory();
		// TODO change this to EncriptionUtil.generateRsaKeys()
		KeyPair keyPair = generateRsaKeys();
		peerMemory.setKeyPair(keyPair);
		logger.debug("Initial peer memory created.");
	}

	@Deprecated
	private static KeyPair generateRsaKeys() {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			logger.error(String.format("Exception during key creation: '%s'",
					e.getMessage()));
		}
		return null;
	}
}
