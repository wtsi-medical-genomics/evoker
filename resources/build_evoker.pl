#!/usr/bin/perl

## Description: script to creates a tar ball of the latest Evoker release
## Usage: ./build_evoker james_morris81 1.1.1
## Input: sourceforge user name and new Evoker version number
## Output: 
## Arguments:
## Author: jm20@sanger.ac.uk

use strict;
use File::Path;

my $release;
my $user;

if (@ARGV == 1) {
	$user    = $ARGV[0];
	$release = $ARGV[1];
		
} else {
	die "Script requires a sourcforge user name and new release name/number\n";
}

## create a release directory
mkdir("evoker_$release");

## checkout the .java files from cvs
system("cvs -z3 -d:ext:$user\@evoker.cvs.sourceforge.net:/cvsroot/evoker checkout src");

## checkout the resource files from cvs
system("cvs -z3 -d:ext:$user\@evoker.cvs.sourceforge.net:/cvsroot/evoker checkout resources");

## move all the xml files for the build
system("cp resources/*.xml ./");

## build the source code
system("ant evoker");
system("ant windows");
system("ant mac");
system("ant clean");

## copy the .jar, .exe and mac app
system("cp -R Evoker* evoker_$release/");

## copy the perl scripts into the bundle directory 
system("cp resources/evoker-helper.pl evoker_$release/");
system("cp resources/int2bnt.pl evoker_$release/");
system("cp resources/illumina_parser.pl evoker_$release/");

## copy the example files
system("cp resources/sample* evoker_$release/");

## checkout the documentation from cvs
system("cvs -z3 -d:ext:$user\@evoker.cvs.sourceforge.net:/cvsroot/evoker checkout docs");

## compile the documentation tex file into a pdf
system("cp docs/evoker-documentation.tex ./");
system("latex evoker-documentation.tex");
system("dvips evoker-documentation.dvi");
system("ps2pdf evoker-documentation.ps");
system("cp evoker-documentation.pdf evoker_$release/");
system("mv evoker_$release/evoker-documentation.pdf evoker_$release/EvokerHelp.pdf");

## tar up the diectory
system("tar -cvf evoker_$release.tar evoker_$release");

## zip up the tar
system("gzip evoker_$release.tar");

## remove all the unwanted files and dirs
rmtree(['src','resources','docs','Evoker.app']);
system("rm evoker-documentation*");
system('rm *.xml');
system("rm Evoker*");
unlink('Evoker.jar');
unlink('Evoker.exe');
