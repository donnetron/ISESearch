#!/bin/bash
source var-config.sh

echo "--- Running update.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

#1. run the java program
#2. post the results to ajax solr
#3. move the files to the posted directory
#4. if posted directory has more than 10 meg of data, zip it as noticedata001.zip and put it in the backup dir

cd $BASE_DIR

#run the crawler
java isespider.ISECrawler -u -n
echo "------------------------------"

#set vars to check .xml file results of crawler
DATA_FILES=($(ls -1 "$DATA_DIR" | grep .xml))
COUNT=${#DATA_FILES[@]}

#check the number of xml files in /data is greater than 0
if [ "$COUNT" -ge 0 ] ; then
	echo Posting $COUNT files...
	#post all the xml files in datadir to solr
	for f in ${DATA_FILES[@]}; do
		echo Posting file $DATA_DIR/$f to $SOLR_URL
		curl $SOLR_URL --data-binary @$DATA_DIR/$f -H 'Content-type:application/xml'
	done
	
	echo ------------------------------
	echo Setting update time: $(date)
	echo ------------------------------
	
	echo $(date) > cache/updated.txt

	#send the commit command to SOLR to make sure all the changes are flushed and visible
	curl "$SOLR_URL?softCommit=true"
	
	echo COMMITTED FILES TO SOLR
	echo ------------------------------

	#make a directory called posted_dir (variable set above) only if it doesn't exist (-p switch)
	mkdir -p $POSTED_DIR
	#move the 'posted to solr' files into a posted sub directory
	for f in ${DATA_FILES[@]}; do
		echo Moving file $DATA_DIR/$f to $POSTED_DIR/$f
		mv --backup=numbered $DATA_DIR/$f $POSTED_DIR/$f
	done
	
	echo MOVED FILES
	echo ------------------------------

fi
echo DONE
