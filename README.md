Evoker
======

Description
-----------
Evoker is a graphical tool for plotting genotype intensity data in order to assess quality of genotype calls. It implements a compact, binary format which allows rapid access to data, even with hundreds of thousands of observations.

Requirements
------------
To run Evoker you will need Java 6.0 (also known as 1.6) or later.

Installing and running
----------------------
To install, download the tarball of the [latest release](https://github.com/wtsi-medical-genomics/evoker/releases). Once extracted, you can either double click Evoker.jar contained within or from the command line:

```
$ java -jar Evoker.jar
```

To run with more memory than the default allocation, the ```-Xmx``` option can be used. For example to specify 1 GB of memeory,

```
$ java -Xmx1024m -jar Evoker.jar
```

For more information see the documentation included in the release.

Note: Version 2.2 has the option of downloading a system specific binary. This is no longer offered in v2.3 as users may encounter issues on their OS with opening applications downloaded from the internet. The jar (Java Archive) files are compatible with all systems and users should not encounter such difficulties.

Building
--------
To build a jar file from source:

1. Clone the repository.
2. Copy ```evoker/resources/build.xml``` into ```evoker/```.
3. Use the command ```ant evoker``` to build the jar file.
4. Use the command ```ant clean``` to remove temporary build files.

Documentation
-------------
A PDF is included in each release tarball. The latex and image files used to produce this are also available in the docs directory.

Website
-------
[http://www.sanger.ac.uk/resources/software/evoker/](http://www.sanger.ac.uk/resources/software/evoker/)

License
-------
MIT License (see LICENSE.md)