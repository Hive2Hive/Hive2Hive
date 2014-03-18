Hive2Hive
=========
Hive2Hive is an open-source library, written in Java, for distributed file synchronization and sharing.

Although many well-known synchronization and sharing services exist, most of them base on centralized client-server approaches and thus store all user data in large external data centers. Regrettably, such private data is often not encrypted and just stored as clear text. This revokes the user’s control over their data as they cannot check who else might have access to it. In addition, such centralized systems suffer from the single-point-of-failure property and hence are vulnerable to targeted attacks. Furthermore, users are bound to these services’ respective pricing and terms of service.

**The Hive2Hive library addresses these issues by providing a free, distributed solution that focuses on maximum security and privacy of both users and data. It supports the whole feature set known from similar centralized approaches, such as Dropbox or Google Drive, all packed in a clean, extendable API.**

Demonstration
-------------
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

File synchronization and sharing operations can then be made us of for logged in users.
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

Advantages
----------
- Supports Whole Feature Set known from Centralized Solutions
- Focus on Security & Anonymity
- P2P Decentralization
  - Scalability
  - Heterogeneity
  - Reliability & Fault-Tolerance
- Platform Independent
- Allows Headless Deployment
- Free & Open-Source
- Highly Extendable
- Detailed Documentation

Documentation
-------------
For the full project documentation, please visit http://www.hive2hive.com/.
The source code itself is thoroughly documented using JavaDoc.

Contribute
----------
The library is intended to be improved and extended so that we all profit from its capabilities. Unlike many other “secure” services, Hive2Hive discloses its implementation and is open for any sort of contribution and constructive criticism.

We believe that everyone can contribute to make Hive2Hive even better! Do you have a suggestion for improvement or an idea for extension? Then you are entirely welcome! Just fork the project and send your pull requests!

As a starting point, you might check the [open issues](https://github.com/Hive2Hive/Hive2Hive/issues?state=open) or just open a new issue to start a discussion around a feature idea or bug.


