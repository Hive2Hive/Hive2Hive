# Hive2Hive
[![Build Status](https://travis-ci.org/Hive2Hive/Hive2Hive.svg?branch=master)](https://travis-ci.org/Hive2Hive/Hive2Hive)

[Hive2Hive](https://github.com/Hive2Hive/Hive2Hive/wiki) is an open-source library, written in Java, for secure, distributed, P2P-based file synchronization and sharing. It is built on top of [TomP2P](https://github.com/tomp2p/TomP2P), an advanced, high-performance DHT for multi-key-value pairs. The Hive2Hive project is licensed under the [MIT License](http://opensource.org/licenses/MIT) and any contribution is welcome.

**Problems of common sync and sharing services**

Although many well-known synchronization and sharing services exist, most of them exhibit the following drawbacks:
* centralized client-server approaches, data resides in large, external data centers
  * single-point-of-failure, vulnerable to targeted attacks
  * often not scalable
* private data is not encrypted
* user control is revoked, no control over who has access to the private data
* user is bound to the respective pricing and terms of service
* no version control, no conflict management

**Hive2Hive is the solution!**

The Hive2Hive library addresses these issues by providing a **free** and **open-sourced**, **distributed** and **scalable** solution that focuses on maximum **security** and **privacy** of both users and data. Aside of this, it supports the whole feature set known from similar centralized approaches, such as *Dropbox*, *OneDrive* or *Google Drive*, and adds functionality for file **versioning** and **conflict management**. All packed in a **clean, simple API**.

There are many simple ways to improve this experience even more. [Start to contribute now!](https://github.com/Hive2Hive/Hive2Hive/wiki/Contribution)

Check our [GitHub Wiki](https://github.com/Hive2Hive/Hive2Hive/wiki) to learn more about [How To Use](https://github.com/Hive2Hive/Hive2Hive/wiki/How-To-Use) and [How It Works](https://github.com/Hive2Hive/Hive2Hive/wiki/How-It-Works).

**Are you looking for a demo application?**

* [PeerWasp](http://www.peerwasp.com/), a Windows-based shell extension à la Dropbox.
* [H2H Console Client](https://github.com/Hive2Hive/Hive2Hive/tree/master/org.hive2hive.client), an executable `.jar` console application.
* [Eclipse RCP Client](https://github.com/Hive2Hive/RCP_Client), an RCP-based client.

### Table of Contents

**[API Demonstration](#api-demonstration)**  
**[Features & Advantages](#features--advantages)**  
**[Installation](#installation)**  
**[Documentation](#documentation)**  
**[Contribution](#contribution)**  
**[Contact](#contact)**

## API Demonstration

The Hive2Hive library provides a simple API that is straightforward to use. ([View Source](https://github.com/Hive2Hive/Hive2Hive/tree/master/org.hive2hive.core/src/main/java/org/hive2hive/core/api))
A short demonstration of the API and its basic usage are given here.

### Network Management

#### Creating a P2P Network

Configuring and setting up a new P2P network is very easy. Just specify the configurations and setup an initial node.

1. The `NetworkConfiguration` and `FileConfiguration` factory classes may help to specify your configurations.
2. Create an initial peer node and connect it.

```java
INetworkConfiguration netConfig = NetworkConfiguration.create("first");
IFileConfiguration fileConfig = FileConfiguration.createDefault();

IH2HNode peerNode = H2HNode.createNode(netConfig, fileConfig);
peerNode.connect();
```

#### Joining an Existing P2P Network

You may want to add other peer nodes to the created network. Any node can join by bootstrapping to another node that is already part of the network.

1. Specify the network configuration for the joining node (i.e., provide the bootstrap address of another node).
2. Create the new node and connect it. It will bootstrap according to its network configuration.

```java
INetworkConfiguration netConfig2 = NetworkConfiguration.create("second", InetAddress.getByName("192.168.1.100"));
IH2HNode peerNode2 = H2HNode.createNode(netConfig2, fileConfig);
peerNode2.connect();
```

### User Management

Once a peer node is connected to a network, users can interact with it. For this, each node provides a user management interface.

1. The user has to provide her credentials.
2. Login the user to the network. If it's the *first* login, she has to register herself, one-time.
3. Then, the user can interact with the network (i.e., file management is enabled).

```java
IUserManager userManager = peerNode.getUserManager();

UserCredentials credentials = new UserCredentials("userId", "password", "pin");
Path rootDirectory = Paths.get("sample/path/to/rootDirectory");

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
IFileManager fileManager = peerNode.getFileManager();

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

#### File Watchdog

In order to keep track of changes in the local file system, a file observer is needed. This observer notifies its attached listeners about all file system events. Either use the provided `H2HFileObserver` and `H2HFileObserverListener` or implement your own observer adhering to the `IFileObserver` and `IFileObserverListener` interfaces.  
The `H2HFileObserverListener` automatically synchronizes the Hive2Hive root folder with the network.

```java
IFileObserverListener listener = new H2HFileObserverListener(fileManager);

IFileObserver observer = new H2HFileObserver(rootDirectory.toFile());
observer.addFileObserverListener(listener);
observer.start();
```

## Features & Advantages

Hive2Hive offers the same basic functionality known from popular synchronization services (e.g., [Dropbox](http://www.dropbox.com)).  
On top of that, Hive2Hive provides additional features such as security and versioning.

* File Synchronization
* File Sharing and Access Permissions *(read/write and read-only)*
* File Versioning and Conflict Management
* File Watchdog / Change Detection *(automated, configurable)*
* [Security](https://github.com/Hive2Hive/Hive2Hive/wiki/Security-Concepts) *(configurable)*
  * [Encryption](https://github.com/Hive2Hive/Hive2Hive/wiki/Security-Concepts#encryption) of files and messages
  * [Authentication](https://github.com/Hive2Hive/Hive2Hive/wiki/Security-Concepts#authentication) of data and messages
* Users can use multiple clients *(simulatenously)*
* Multiple users can use the same machine *(simultaneously)*

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

There are three easy ways to get and include the Hive2Hive library into your project.

If you just want to use the library, either refer to option 1 or 2.  
If you want to [contribute to the project](#contribution), please refer to option 3.
- **Option 1: Add Maven dependency** *(recommended)*  
  You can add the latest stable release as an [Apache Maven](http://maven.apache.org/) dependency. Add the following to your `pom.xml` and make sure to select the most recent version.  
```xml
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

- For more details and documentation of the Hive2Hive project, please visit the [GitHub Wiki](https://github.com/Hive2Hive/Hive2Hive/wiki).
- The source code itself is thoroughly documented using JavaDoc.

## Contribution

The library is intended to be improved and extended so that we all profit from its capabilities. Unlike many other “*secure*” services, Hive2Hive discloses its implementation and is open for any sort of contribution and constructive criticism.

**We believe that everyone can contribute to make Hive2Hive even better!** 

There are several things - from simple to complex - you can do to help:
- [Star](https://github.com/Hive2Hive/Hive2Hive/stargazers) and [watch](https://github.com/Hive2Hive/Hive2Hive/watchers) the project here on GitHub
- Spread the word and help us getting attention (e.g., follow/tweet about [@Hive2Hive](https://twitter.com/Hive2Hive))
- Suggest and post **your ideas** about improvements or extensions on the [issues](https://github.com/Hive2Hive/Hive2Hive/issues?state=open) page
- Participate in [discussions](https://github.com/Hive2Hive/Hive2Hive/issues?labels=&page=1&state=open), share your expertise
- Help us with the documentation: JavaDoc, [GitHub Wiki](https://github.com/Hive2Hive/Hive2Hive/wiki), etc.
- Help us with the implementation of (your) features
- **Fork the project** and send your pull requests
- Help the community by answering questions on StackOverflow (tagged with [`hive2hive`](http://stackoverflow.com/questions/tagged/hive2hive))

**Also, if you are a professional cryptographer with interest in this project, any feedback on the project is very welcome!**
