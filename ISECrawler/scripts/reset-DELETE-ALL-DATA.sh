#!/bin/bash
cd ..
rm -rf backup/*
rm -rf cache/*
rm -rf conf/*
rm -rf data/*
mkdir data/incomplete/
rm -f logs/*.log
rm -f isespider/*.class
javac -d . src/isespider/ISECrawler.java