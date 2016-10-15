#!/bin/bash
source var-config.sh

echo "--- Running index-rebuild.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

cd $BASE_DIR
pwd

#set up index-rebuild directories
INDEX_DIR=$BASE_DIR/index-rebuild

#unzip all xml-data files

mkdir -p $INDEX_DIR
mkdir -p $INDEX_DIR/unmerged
mkdir -p $INDEX_DIR/merged

echo 7z x -y -o$INDEX_DIR/unmerged/ backup/noticedata\*.7z
7z x -y -o$INDEX_DIR/unmerged/ backup/noticedata\*.7z

echo /usr/bin/java -cp .:lib/* isespider.XMLNoticeHelper -m -i \"$INDEX_DIR/unmerged/*.xml\" -o \"$INDEX_DIR/merged/merged.xml\"
/usr/bin/java -cp .:lib/* isespider.XMLNoticeHelper -m -i \"$INDEX_DIR/unmerged/*.xml\" -o \"$INDEX_DIR/merged/merged.xml\"

echo /usr/bin/java -cp .:lib/* isespider.XMLNoticeHelper -sf \"$INDEX_DIR/merged/*.xml\" -o \"$INDEX_DIR/index-rebuild.xml\"
/usr/bin/java -cp .:lib/* isespider.XMLNoticeHelper -i \"$INDEX_DIR/merged/*.xml\" -sf 1,048,576 -o \"$INDEX_DIR/index-rebuild.xml\"

# delete all documents from the solr index
curl $SOLR_URL/update --data '<delete><query>*:*</query></delete>' -H 'Content-type:text/xml; charset=utf-8'
curl $SOLR_URL/update --data '<commit/>' -H 'Content-type:text/xml; charset=utf-8'

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

	echo ------------------------------
	echo UPDATING STATISTICS
	echo ------------------------------
	
	./$SCRIPT_DIR/statistics.sh

fi
echo DONE