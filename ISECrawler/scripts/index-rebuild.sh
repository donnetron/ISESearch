#!/bin/bash
source var-config.sh

echo "--- Running index-rebuild.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

./reset-SOLR.sh

INDEX_DIR="$BASE_DIR"/index-rebuild

#set vars to check .xml file results
DATA_FILES=($(ls -1 "$INDEX_DIR" | grep .xml))
COUNT=${#DATA_FILES[@]}

#check the number of xml files in /data is greater than 0
if [ "$COUNT" -ge 0 ] ; then
	echo Posting $COUNT files...
	#post all the xml files in datadir to solr
	for f in ${DATA_FILES[@]}; do
		echo Posting file $INDEX_DIR/$f to $SOLR_URL/update
		echo curl $SOLR_URL/update --data-binary @$INDEX_DIR/$f -H 'Content-type:application/xml'
		curl $SOLR_URL/update --data-binary @$INDEX_DIR/$f -H 'Content-type:application/xml'
	done
	
	#send the commit command to SOLR to make sure all the changes are flushed and visible
	echo curl "$SOLR_URL?softCommit=true"
	curl "$SOLR_URL?softCommit=true"
	
	echo COMMITTED FILES TO SOLR
	echo ------------------------------

fi
echo DONE