#!/usr/bin/perl

## Description: script to create a tar ball of the latest Evoker release
## Usage: ./build_evoker james_morris81 1.1.1
## Input: sourceforge user name and new Evoker version number
## Author: jm20@sanger.ac.uk

use strict;
use File::Path;

my $release;
my $user;

if (@ARGV == 2) {
	$user    = $ARGV[0];
	$release = $ARGV[1];
		
} else {
	die "Script requires a sourcforge user name and new release name/number\n";
}

## create a release directory and platform sub directories
mkdir("evoker_$release");
mkdir("evoker_$release/win");
mkdir("evoker_$release/mac");
mkdir("evoker_$release/other");
mkdir("evoker_$release/all");

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

## checkout the documentation from cvs
system("cvs -z3 -d:ext:$user\@evoker.cvs.sourceforge.net:/cvsroot/evoker checkout docs");

## compile the documentation tex file into a pdf
system("cp docs/* ./");
system("pdflatex evoker-documentation.tex");
system("pdflatex evoker-documentation.tex");

## copy the .jar, .exe and mac app
system("cp -R Evoker* evoker_$release/all/");
system("cp -R Evoker.exe evoker_$release/win/");
system("cp -R Evoker.app evoker_$release/mac/");
system("cp -R Evoker.jar evoker_$release/other/");

 for my $platform ('all','win','mac','other') {

	system("cp resources/evoker-helper.pl evoker_$release/$platform/");
	system("cp resources/int2bnt.pl evoker_$release/$platform/");
	system("cp resources/illumina_parser.pl evoker_$release/$platform/");
	system("cp resources/sample* evoker_$release/$platform/");
	system("cp evoker-documentation.pdf evoker_$release/$platform/");
	system("mv evoker_$release/$platform/evoker-documentation.pdf evoker_$release/$platform/EvokerHelp.pdf");
	system("tar -cvf evoker_$release/evoker_$release\_$platform.tar evoker_$release/$platform/");
	system("gzip evoker_$release/evoker_$release\_$platform.tar");
			
}

## remove all the unwanted files and dirs
rmtree(['src','resources','docs','Evoker.app']);
system("rm evoker-documentation*");
system("rm *.png");
system('rm *.xml');
system("rm Evoker*");
unlink('Evoker.jar');
unlink('Evoker.exe');
