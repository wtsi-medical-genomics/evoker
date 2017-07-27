Evoker
======

Description
-----------
Evoker is a graphical tool for plotting genotype intensity data in order to assess quality of genotype calls. It implements a compact, binary format which allows rapid access to data, even with hundreds of thousands of observations.

Evoker consists of two components:

* A Java desktop application to be used on a local machine
* A perl script `evoker-helper.pl` which will reside on the server where your intensity and genotype data is stored. This script reads small slices of your intensity and genotype files relevant to the SNP of interest and transfers this to the Java application over SSH.

UK Biobank v2
-------------
Evoker has been adapted to view UK Biobank v2 data (released July 2017). Evoker expects the UK Biobank files to have the same naming conventions from the original data release. In a directory the following must all exist together:

For each chromosome of interest, the following files must all sit in the same directory:

```
ukb_cal_chr{chromosome}_v2.bed
ukb_snp_chr{chromosome}_v2.bim
ukb_int_chr{chromosome}_v2.bin
```

In addition, you must point Evoker to the original fam file (the batch information in the final column).

#### UK Biobank v2 steps

1. [Install Evoker and remote helper script](https://github.com/wtsi-medical-genomics/evoker#installing)
2. On local machine, [open Evoker](https://github.com/wtsi-medical-genomics/evoker#running)
3. `File` > `Connect to remote server`
4. Select `UK Biobank v2` file format. Then enter:

    * `Host` the remote server hostname
    * `Port` port to SSH to (default 22)
    * `Remote directory` the absolute path where the UK Biobank files reside.
    * `Local directory` a local location where temporary data slices can be stored.
    * `Username` your username on the remote host
    * `Password` your password on the remote host
    * `Remote FAM file` the location on the remote machine of the `fam` file provided to you by UK Biobank (including the final column which lists the batches).
    * `Remote temp directory` it is assumed you will not have write access to the release directory (instituions will most likely share a single release) so please specify a directory you have read/write access to where temporary subsets of the intensity/plink data can be stored on the remote machine.

5. Click OK to start transferring the fam and any bim files in the remote directory to your local machine. The speed of this process will depend on your data connection.
6. Enter the SNP of interest (rsid from the bim file) to view.
7. Scroll up and down to view all of the batches. If desired sort on Batch name, MAF, HWE p-value, or GPC from the `View` > `Sort` menu.

**Note**: at present it is not possible to re-call (with the lasso select) UK Biobank v2 data at the moment. This feature will be available in the next release.



Maintainer
----------
Daniel Rice (dr9@sanger.ac.uk)

Authors
-------
* James Morris
* Jeff Barrett

Contributors
------------
* Tim Poterba
* Natalie Wirth
* Daniel Rice

Requirements
------------
* Desktop application: Java 8.0 (also known as 1.8) or later.
* Remote helper script `evoker-helper.pl`: Perl 5

Installing
----------
#### Desktop application
Download and extract the tarball of the [latest release](https://github.com/wtsi-medical-genomics/evoker/releases) on your local machine.

#### Remote helper script (`evoker-helper.pl`)
To view data that is on a remote machine (ie a UNIX server), download `evoker-helper.pl` and add it to your path so that it is executable everywhere. If using bash, the following will download this into a folder in your home directory:

```bash
mkdir ~/evoker-helper
curl -o ~/evoker-helper/evoker-helper.pl https://raw.githubusercontent.com/wtsi-medical-genomics/evoker/master/src/resources/evoker-helper.pl
chmod 777 ~/evoker-helper/evoker-helper.pl
echo export PATH=\"~/evoker-helper:\$PATH\" >> ~/.bashrc
. ~/.bashrc
```

To test that it is working go to some (non-home directory) location:

```bash
cd /
evoker-helper.pl --version
```

and you should see the version reported to you.


Running
-------
Within the untarred release directory you can either double click Evoker.jar contained within or from the command line:

```
$ java -jar Evoker.jar
```

To run with more memory than the default allocation, the ```-Xmx``` option can be used. For example to specify 1 GB of memeory,

```
$ java -Xmx1024m -jar Evoker.jar
```

For more information see the documentation included in the release.

Documentation
-------------
A PDF is included in each release tarball. The latex and image files used to produce this are also available in the docs directory.

Building
--------
To build a jar file from source:

1. Clone the repository.
2. Copy ```evoker/resources/build.xml``` into ```evoker/```.
3. Use the command ```ant evoker``` to build the jar file.
4. Use the command ```ant clean``` to remove temporary build files.

Todo
----
[ ] Exclude individuals with a negative number as their sample ID.
[ ] Deal with hidden files (eg `.samples.fam`).
[ ] Gracefully fail if evoker-helper.pl is not reachable at the remote server.
[ ] Export BED changes when viewing over a remote connection.
[ ] Plot SNP Posterior ellipses.
[ ] Save the plot array to remove the need to re-load all data on sorting.

Citation
--------
James A. Morris, Joshua C. Randall, Julian B. Maller, Jeffrey C. Barrett; Evoker: a visualization tool for genotype intensity data. Bioinformatics 2010; 26 (14): 1786-1787. doi: 10.1093/bioinformatics/btq280

Website
-------
[http://www.sanger.ac.uk/science/tools/evoker](http://www.sanger.ac.uk/science/tools/evoker)

License
-------
MIT License (see LICENSE.md)