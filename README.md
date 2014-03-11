Hive2Hive
=========

For the full documentation, please visit our website at http://www.hive2hive.com.

What Is Hive2Hive?
------------------

The basic intention behind the Hive2Hive project is to provide a free and easy-to-use library that supports operations or tasks which shall be executed in a decentralized manner while focussing on maximal security. Although many well-known services run in centralized, server-based environments, it is a legitimate question for many applications to consider a decentralized implementation for certain services or operations. With Hive2Hive, such decentralization is achieved by building functionalities on top of an underlying peer-to-peer (P2P) network structure. The library takes full responsibility of the network interaction and thus provides the necessary level of abstraction.
The main focus of the library’s current state lies on user and file management. Concretely, an extensive amount of fundamental operations is provided for applications that desire to store, backup, synchronize or share files in and over the network. However, the Hive2Hive library is designed to be easily extendable for other services that intend to profit from decentralized properties and, at the same time, enrich its set of supported operations.

Why Using Hive2Hive?
--------------------

The overall mission of Hive2Hive is to keep things decentralized and thus avoids any central elements. Such a system has many advantages over centralized client-server approaches. In contrast to other (distributed) solutions, Hive2Hive puts much effort on security so as to ensure not only the data in the network, but also the privacy of each and every user. Unlike many other closed-source services that claim to be secure, the Hive2Hive library fosters transparency and discloses its implementation details while profiting from open-source community feedback.

- P2P Decentralization
  - Scalability
  - Heterogeneity
  - Reliability & Fault-Tolerance
- Focus on Security & Anonymity
- Platform Independent
- Free & Open-Source
- Highly Extendable
- Detailed Documentation

The Role of Hive2Hive
---------------------

As stated above, the required decentralization comes from the peer-to-peer approach on which Hive2Hive is built upon. Essentially, the library provides ways to quickly create peers/nodes that are connected to the network and represent handles by which specific operations can be triggered to interact with other peers in the network. Since the Hive2Hive library takes care of such network interaction, it exhibits itself as a layer between the application and the underlying peer-to-peer network.

In order to deal with the peer-to-peer overlay internally, Hive2Hive makes use of TomP2P (http://www.tomp2p.net), one of the most advanced open-source implementations of a distributed hash table (DHT).
