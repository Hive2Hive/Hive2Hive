The Hive2Hive library is based on a distributed hash table (DHT), a concept to store data in a decentralized manner. Every client that uses the library is an autonomous peer in the network. All peers together are forming a ring or a tree. A DHT in its easiest form has two functions: put and get. Data then is distributed over the network and stored at the responsible peer. The library uses TomP2P, one of the most advanced open-source implementation of a DHT.
Hive2Hive is an additional layer on top of the DHT specialized in file synchronization and sharing. The library works without any central instance but has similar features as traditional centralized services (Dropbox, Google Drive or Skydrive). DHT's have many advantages over centralized systems like scalability, reliability and anonymity.

How to use it?
Bind the Hive2Hive library and all necessary dependencies into your project. The dependent libraries are not part of Hive2Hive and can also be imported using maven or other tools.
On the project website you can see a usage guide.

For more information, see http://hive2hive.com/