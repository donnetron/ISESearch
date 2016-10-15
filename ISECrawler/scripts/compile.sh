#!/bin/bash
source var-config.sh

echo "--- Running compile.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

cd $BASE_DIR
rm -f isespider/*.class
javac -d . src/isespider/ISECrawler.java $*
javac -d . src/isespider/XMLNoticeHelper.java $*
