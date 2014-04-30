# ![Hive2Hive logo](http://hive2hive.com/wp-content/uploads/2014/04/Hive2Hive_Icon-e1398611873118.png) Hive2Hive

[Hive2Hive](http://www.hive2hive.org) is an open-source library, written in Java, for secure, distributed, P2P-based file synchronization and sharing. It is built on top of [TomP2P](https://github.com/tomp2p/TomP2P), which is an advanced, high-performance DHT for multi-key-value pairs. The Hive2Hive project is licensed under the [MIT License](http://opensource.org/licenses/MIT) and any contribution is welcome.

Although many well-known synchronization and sharing services exist, most of them base on centralized client-server approaches and thus store all user data in large external data centers. Regrettably, such private data is often not encrypted and just stored as clear text. This revokes the user’s control over their data as they cannot check who else might have access to it. In addition, such centralized systems suffer from the single-point-of-failure property and hence are vulnerable to targeted attacks. Furthermore, users are bound to these services’ respective pricing and terms of service.

**The Hive2Hive library addresses these issues by providing a free, distributed solution that focuses on maximum security and privacy of both users and data. It supports the whole feature set known from similar centralized approaches, such as Dropbox or Google Drive, all packed in a clean API.**

### Table of Contents

**[API Demonstration](#api-demonstration)**  
**[Features & Advantages](#features--advantages)**  
**[Installation](#installation)**  
**[Documentation](#documentation)**  
**[Contribution](#contribution)**  
**[Contact](#contact)**

## API Demonstration

A short demonstration of the API and its basic usage are given here. ([see more](http://hive2hive.com/?page_id=429))
### Network Management

**Create P2P Network**  
Configuring and setting up a new P2P network is very easy. Just specify the configurations and setup an initial node.

1. `NetworkConfiguration` and `FileConfiguration` factory classes may help to specify your configurations
2. create the initial node and connect it

```java
INetworkConfiguration netConfig = NetworkConfiguration.create("first");
IFileConfiguration fileConfig = FileConfiguration.createDefault();

IH2HNode node = H2HNode.createNode(netConfig, fileConfig);
node.connect();
```

**Join Existing P2P Network**  
You may want to add other nodes to your created network. Any node can join by bootstrapping to another node that is already part of the network.

1. specify the network configuration for the joining node (i.e., provide bootstrap address of another node)
2. create the new node and connect it (it will bootstrap according to its network configuration)

```java
INetworkConfiguration netConfig2 = NetworkConfiguration.create("second", InetAddress.getByName("192.168.1.100"));
IH2HNode node2 = H2HNode.createNode(netConfig2, fileConfig);
node2.connect();
```

### User Management

Once a node is connected to a network, users can interact with it. For this, each node provides a user management interface.

1. user has to provide its credentials
2. login user (if a user is new to the network, she has to register on her first visit)
3. user can interact with the network (i.e., file management is enabled)

```java
IUserManager userManager = node.getUserManager();

UserCredentials credentials = new UserCredentials("userId", "password", "pin");
Path rootDirectory = Paths.get("C:/User/XYZ/...");

if (!userManager.isRegistered(credentials.getUserId())) {
	userManager.register(credentials).await();
}
userManager.login(credentials, rootDirectory).await();
```

### File Management

As soon as a user is logged in to the network, her files are automatically synchronized with the current node. Many further file operations are available. For this, each node provides a file management interface.
- **add** / **delete** file
- **update** / **recover** file
- **share** file with another user
- **move** file

```java
IFileManager fileManager = node.getFileManager();

File folder = new File("folderpath");
File file = new File(folder, "filepath");

fileManager.add(file);

fileManager.update(file);

fileManager.share(folder, "other-userId", PermissionType.WRITE);

IVersionSelector versionSelector = new IVersionSelector() {
	@Override
	public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
		return availableVersions.get(0);
	}
};
fileManager.recover(file, versionSelector);

fileManager.move(folder, new File("other-folder"));

fileManager.delete(file);
```

### File Watchdog

In order to keep track of changes in the local file system, a file observer is needed. This observer then notifies its attached listeners on all file system events. You can either use the provided `H2HFileObserver` and `H2HFileObserverListener` or implement your own adhering to the `IFileObserver` and `IFileObserverListener` interfaces.  
The `H2HFileObserverListener` automatically synchronizes the Hive2Hive root folder with the network.

```java
IFileObserverListener listener = new H2HFileObserverListener(fileManager);

IFileObserver observer = new H2HFileObserver(rootDirectory.toFile());
observer.addFileObserverListener(listener);
observer.start();
```

## Features & Advantages

Hive2Hive offers the same basic functionality known from popular synchronization services. (e.g., [Dropbox](http://www.dropbox.com))  
On top of that, Hive2Hive provides additional features such as security and versioning.

- File Synchronization
- File Sharing (including user permissions (*write*, *read-only*))
- File Versioning (including conflict detection)
- File Watchdog / Change Detection (automated, configurable)
- Security (configurable, [see more](http://hive2hive.com/security-aspects/))
  - Encryption of files
  - Encryption of messages
  - Authenticity of data and messages
- Users can use multiple clients (simulatenously)
- Multiple users can use the same machine (simultaneously)

**Using the Hive2Hive library is very simple and has several advantages:**
- P2P Decentralization
  - Scalability
  - Heterogeneity
  - Reliability & Fault-Tolerance
- no file size limits (configurable)
- platform independent (JVM)
- headless deployment possible<sup>*</sup> (e.g., on a [Raspberry Pi](http://www.raspberrypi.org/))  
- free & open-source
- generously configurable & customizable
- highly extendable
- detailed documentation

<sup>*</sup> Try our console-based [`org.hive2hive.client`](https://github.com/Hive2Hive/Hive2Hive/tree/master/org.hive2hive.client) by just executing the library `.jar`.

**And there is even more to come:**
- A demonstrative GUI client is waiting in the wings ([see more](https://github.com/Hive2Hive/RCP_Client))
- REST API ([see more](https://github.com/Hive2Hive/Hive2Hive/issues/68))
- Secure Bootstrapping ([see more](https://github.com/Hive2Hive/Hive2Hive/issues/61))
- LAN Synchronization ([see more](https://github.com/Hive2Hive/Hive2Hive/issues/77))
- Large File Alternatives ([see more](https://github.com/Hive2Hive/Hive2Hive/issues/73))
- Same Network - Different Applications ([see more](https://github.com/Hive2Hive/Hive2Hive/issues/80))
- and [more](https://github.com/Hive2Hive/Hive2Hive/issues?labels=future+work&page=1&state=open)

## Installation

There are three easy ways to get and include the Hive2Hive library into your project. ([more](http://hive2hive.com/download/))

If you just want to use the library, either refer to option 1 or 2.  
If you want to [contribute to the project](#contribution), please refer to option 3.
- **Option 1: Add Maven dependency** *(recommended)*  
  You can add the latest stable release as an [Apache Maven](http://maven.apache.org/) dependency and fetch it from our repository. Add the following to your `pom.xml` and make sure to select the most recent version.  
```xml
  <repository>
    <id>hive2hive.org</id>
    <url>http://repo.hive2hive.org</url>
  </repository>
  ...
  <dependency>
    <groupId>org.hive2hive</groupId>
    <artifactId>org.hive2hive.core</artifactId>
    <version>1.X.X</version>
  </dependency>
```
- **Option 2: Add JAR-file directly**  
  In case you don't want to use Maven, you can just download the [latest stable release](https://github.com/Hive2Hive/Hive2Hive/releases) that comes directly with all necessary sources. All required `.jar`-files are packed and delivered to you as a `.zip`.
- **Option 3: Clone from GitHub**  
  If you want to contribute to the Hive2Hive library project, this is what you should do. Cloning from GitHub gets the *bleeding edge* of development and thus some sources might not be stable. So this option is not recommended if you just want to use the library.

## Documentation

For more details and documentation about the project, please visit http://www.hive2hive.com/.

The source code itself is thoroughly documented using JavaDoc.

## Contribution

The library is intended to be improved and extended so that we all profit from its capabilities. Unlike many other “*secure*” services, Hive2Hive discloses its implementation and is open for any sort of contribution and constructive criticism.

**We believe that everyone can contribute to make Hive2Hive even better!** 

There are several things - from simple to complex - you can do to help:
- [watch](https://github.com/Hive2Hive/Hive2Hive/watchers) and/or [star](https://github.com/Hive2Hive/Hive2Hive/stargazers) the project here on GitHub
- help us getting attention (e.g., follow/tweet about [@Hive2Hive](https://twitter.com/Hive2Hive))
- suggest and post your ideas about improvements or extensions on the [issues](https://github.com/Hive2Hive/Hive2Hive/issues?state=open) page
- participate in the [discussions](https://github.com/Hive2Hive/Hive2Hive/issues?labels=&page=1&state=open), share your expertise
- help us with the implementation of (your) features
- fork the project and send your pull requests
- help the community by answering questions on StackOverflow (tagged with [`hive2hive`](http://stackoverflow.com/questions/tagged/hive2hive))

Also, if you are a professional cryptographer with interest in this project, any feedback on the project is very welcome.

## Contact

If you have any questions, feel uncomfortable or uncertain about an issue or your changes, feel free to reach us via email at [info@hive2hive.com](mailto:info@hive2hive.com). Please consider posting your question on StackOverflow (using the [`hive2hive`](http://stackoverflow.com/questions/tagged/hive2hive) tag) in case it is a technical question that might interest other developers, too.

We provide you with all information you need and will happily help you via email, Skype, remote pairing or whatever you are comfortable with.
