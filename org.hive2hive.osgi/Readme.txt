This project serves as a container for all libraries the Hive2Hive core is dependent on.
This allows a client to use the Hive2Hive core project as an OSGI[1] bundle. 

If dependencies of Hive2Hive (direct as well as transient ones) are updated, the changes
have to be reflected (manually) in this project. The following steps explain the process
in detail:

1.	Changing a dependency of one of the Hive2Hive components or its dependent libraries
		As Hive2Hive is built using maven, dependencies are generally managed through 
		the corresponding .pom files of the projects. So the first step is to update the
		.pom file. Changes such as a new/other version of a library need to be reflected
		in this project by manually copying the corresponding library into this project
		and adjusting the configuration accordingly. This holds for any dependency, even
		those of transitively referenced libraries!
		
2.	Copying the corresponding library into the lib folder of this project
		The simplest way of getting the library is by updating the .pom file and running
		a maven build, then copying the needed library from the local maven repository.
		
3.	Updating the MANIFEST.MF
		The new/changed library needs to be added to the classpath (and in the case of a
		change the old library needs to be removed from it). In the MANIFEST.MF the
		“Bundle-ClassPath” needs to be updated to reflect the change. Further the
		“Export-Package” needs to be updated as well by removing obsolete packages and
		adding the new ones.

[1] http://www.osgi.org
