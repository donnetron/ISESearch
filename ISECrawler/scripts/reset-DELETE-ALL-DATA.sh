#!/bin/bash
source var-config.sh

echo "--- Running reset-DELETE-ALL-DATA.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

cd $BASE_DIR

rm -rf backup/*
rm -rf cache/*
rm -rf conf/*
rm -rf data/*
mkdir data/incomplete/
rm -f logs/*.log
rm -f isespider/*.class
javac -d . src/isespider/ISECrawler.java