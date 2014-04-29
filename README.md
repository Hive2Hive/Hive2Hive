# ![Hive2Hive logo](http://hive2hive.com/wp-content/uploads/2014/04/Hive2Hive_Icon-e1398611873118.png) Hive2Hive

[Hive2Hive](http://www.hive2hive.org) is an open-source library, written in Java, for distributed, P2P-based file synchronization and sharing.  
It is built on top of [TomP2P](http://tomp2p.net/), which is an advanced, high-performance DHT for multi-key-value pairs. The Hive2Hive project is licensed under the [MIT License](http://opensource.org/licenses/MIT) and any contribution is welcome.

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

Configuring and setting up a P2P network is very easy.
```java
// define configuration objects
INetworkConfiguration nodeConfig = NetworkConfiguration.create("nodeID", InetAddress.getByName("192.168.1.100"));
IFileConfiguration fileConfig = FileConfiguration.createDefault();

// create peer
IH2HNode node = H2HNode.createNode(nodeConfig, fileConfig);
node.connect();
```
Users can then be announced to the created P2P network. Once announced, they can login/logout to/from the network whenever they want and with whatever client they use. For security reasons, each user has to provide his/her credentials for these operations.
```java
IUserManager userManager = node.getUserManager();

UserCredentials credentials = new UserCredentials("userID", "password", "pin");

// announce the user
userManager.register(credentials);
        
// login the user and provide the local root directory path
userManager.login(credentials, Paths.get("C:\User\XYZ\..."));
```

File synchronization and sharing operations can then be made use of.
```java
IFileManager fileManager = node.getFileManager();
        
File folder = new File("demo-folder");
File file = new File(folder, "demo-file");
        
// add a file
fileManager.add(file);
        
// share a folder with another user (write permission)
fileManager.share(folder, "otherUser", PermissionType.WRITE);
        
// update a file
fileManager.update(file);
        
// recover a file's other version
IVersionSelector versionSelector = new IVersionSelector() {
    @Override
    public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
        return availableVersions.get(0);
    }
};
fileManager.recover(file, versionSelector);
 
// move a file in the file hierarchy
File otherFolder = new File("other-demo-folder");
fileManager.move(folder, otherFolder);
 
// delete a file
fileManager.delete(file);
```

See [here](http://hive2hive.com/?page_id=429) for more detailed information about the API.

## Features & Advantages

Hive2Hive offers the same basic functionality known from popular synchronization services. (e.g., [Dropbox](http://www.dropbox.com))  
On top of that, Hive2Hive provides additional features such as security or versioning.

- File Synchronization (folder based)
- File Sharing
- File Versioning (including conflict detection)
- File Observation (automated, configurable)
- Security (configurable, [see more](http://hive2hive.com/security-aspects/))
  - Encryption of files and network communication
  - Authenticity of data and messages
- Users may use multiple clients (simulatenously)
- Multiple users may use the same machine (simultaneously)

**Using the Hive2Hive library is very simple and has several advantages:**
- P2P Decentralization
  - Scalability
  - Heterogeneity
  - Reliability & Fault-Tolerance
- no file size limits (configurable)
- runs on Windows, OS X and Linux
- headless deployment possible<sup>*</sup> (e.g., on a [Raspberry Pi](http://www.raspberrypi.org/))  
- free & open-source
- freely configurable
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
