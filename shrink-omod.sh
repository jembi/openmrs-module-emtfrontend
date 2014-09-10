#!/bin/bash

rm -rf 1
mkdir 1
cd 1
jar xf ../omod/target/emtfrontend-0.6-SNAPSHOT.omod 
mkdir ../2
cd ../2
jar xf ../1/lib/pdfbox-1.8.5.jar 
rm -rf org/apache/pdfbox/resources/cmap/
jar cf ../1/lib/pdfbox-1.8.5.jar .
cd ../1
jar cf ../omod/target/emtfrontend-0.6-SNAPSHOT.omod .
cd ..
rm -rf 1 2

