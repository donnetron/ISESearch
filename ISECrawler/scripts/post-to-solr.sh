#!/bin/bash
source var-config.sh

echo "--- Running post-to-solr.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

#set vars to check .xml file results
DATA_FILES=($(ls -1 "$DATA_DIR" | grep .xml))
COUNT=${#DATA_FILES[@]}

#check the number of xml files in /data is greater than 0
if [ "$COUNT" -ge 0 ] ; then
	echo Posting $COUNT files...
	#post all the xml files in datadir to solr
	for f in ${DATA_FILES[@]}; do
		echo Posting file $DATA_DIR/$f to $SOLR_URL
		echo curl $SOLR_URL --data-binary @$DATA_DIR/$f -H 'Content-type:application/xml'
		curl $SOLR_URL --data-binary @$DATA_DIR/$f -H 'Content-type:application/xml'
	done
	
	#send the commit command to SOLR to make sure all the changes are flushed and visible
	echo curl "$SOLR_URL?softCommit=true"
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