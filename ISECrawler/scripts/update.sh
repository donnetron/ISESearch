#!/bin/bash
#0. set class path?
#1. run the java program
#2. post the results to ajax solr
#3. move the files to the posted directory
#4. if posted directory has more than 10 meg of data, zip it as noticedata001.zip and put it in the backup dir

# note change to parent dir here!
cd ..

CLASSPATH=:lib/*:.

BACKUP_DIR=backup
DATA_DIR=data
POSTED_BACKUP_ZIP=noticedata
POSTED_DIR=data/posted
DUPLICATE_DIR=data/posted/duplicate
URL=http://localhost:8983/solr/update


#run the crawler
java isespider.ISECrawler -u -n
#java isespider.ISECrawler
echo "------------------------------"

#set vars to check .xml file results of crawler
DATA_FILES=($(ls -1 "$DATA_DIR" | grep .xml))
COUNT=${#DATA_FILES[@]}

#check the number of xml files in /data is greater than 0
if [ "$COUNT" -ge 0 ] ; then
	echo Posting $COUNT files...
	#post all the xml files in datadir to solr
	for f in ${DATA_FILES[@]}; do
		echo Posting file $DATA_DIR/$f to $URL
		curl $URL --data-binary @$DATA_DIR/$f -H 'Content-type:application/xml'
	done
	
	echo ------------------------------
	echo Setting update time: $(date)
	echo ------------------------------
	
	echo $(date) > cache/updated.txt

	#send the commit command to SOLR to make sure all the changes are flushed and visible
	curl "$URL?softCommit=true"
	
	echo COMMITTED FILES TO SOLR
	echo ------------------------------

	#make a directory called posted_dir (variable set above) only if it doesn't exist (-p switch)
	mkdir -p $POSTED_DIR
	mkdir -p $DUPLICATE_DIR
	#move the 'posted to solr' files into a posted sub directory
	for f in ${DATA_FILES[@]}; do
		echo Moving file $DATA_DIR/$f to $POSTED_DIR/$f
		mv --backup=numbered $DATA_DIR/$f $POSTED_DIR/$f
	done
	
	echo MOVED FILES
	echo ------------------------------
	
	POSTED_DIR_SIZE=$(du -m --max-depth=0 $POSTED_DIR | cut -f1)
	#if the size of the posted dir is more than 10MB
	if [ $POSTED_DIR_SIZE -ge 10 ] ; then
		#check the files named 'noticedataXX.zip' to get the highest numbered file
		FILECOUNT=$(ls -1 "backup/${POSTED_BACKUP_ZIP}"* | egrep -o '[0-9]+' | sort -rn | head -n 1)

		#zip the contents of the posted dir into a highest numbered + 1 zip file in the backup dir
		zip -jm $BACKUP_DIR/$POSTED_BACKUP_ZIP$(($FILECOUNT +1)).zip $POSTED_DIR/*.xml
		
		echo BACKUP PERFORMED TO: $BACKUP_DIR
		echo ------------------------------
	fi
fi
echo DONE
