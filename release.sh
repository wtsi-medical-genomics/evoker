#!/usr/bin/env bash
cp resources/build.xml .
ant evoker
ant clean
rm -fr release build.xml
mkdir evoker
cp -r Evoker.jar docs/evoker-documentation.pdf src/resources/ resources/test-files evoker
rm -r evoker/.[!.]*
tar zcvf evoker_2.4.tar.gz evoker