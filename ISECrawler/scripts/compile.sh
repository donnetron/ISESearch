#!/bin/bash
cd ..
rm -f isespider/*.class
javac -d . src/isespider/ISECrawler.java $*
