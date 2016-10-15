#!/bin/bash
source /home/don/ISESearch/ISECrawler/scripts/var-config.sh

echo "--- Running backup.sh ---"
# echo --- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---

#1. if tomorrow its the last day of a month
#2. extract the curent noticedata backup zip and make it the first file in the POSTED dir
#3. merge all files in the posted dir into one and zip it up, then delete them
#4. add this month's cache files to the cache zip
#5. upload to dropbox

cd $BASE_DIR

#if tomorrow is the first of the month (today is the last day of the month), then do an incremental merge/zip/upload
if [ $(date --date='tomorrow' +%d) -eq 1 ] ; then
	echo "Last day of month: performing backup..."
	
	if [ -f "$BACKUP_XML_ZIP" ] ; then
		echo 7z e "$BACKUP_XML_ZIP" -o"$BACKUP_DIR"
		7z e "$BACKUP_XML_ZIP" -o"$BACKUP_DIR"

		echo mv "$BACKUP_XML_FILE" "$POSTED_DIR"/0000.xml
		mv "$BACKUP_XML_FILE" "$POSTED_DIR"/0000.xml
	fi
	
	echo /usr/bin/java -cp .:lib/* isespider.XMLNoticeHelper -m -i \""$POSTED_DIR"/*.xml\" -o \""$BACKUP_XML_FILE"\"
	/usr/bin/java -cp .:lib/* isespider.XMLNoticeHelper -m -i \""$POSTED_DIR"/*.xml\" -o \""$BACKUP_XML_FILE"\"

	#echo rm -f "$POSTED_DIR"/*
	#rm -f "$POSTED_DIR"/*

	echo mv "$POSTED_DIR"/* "$POSTED_DIR"/temp_backup
	mv "$POSTED_DIR"/* "$POSTED_DIR"/temp_backup

	# first zip up the XML file in a new 7z file, then delete the XML file
	echo 7z a "$BACKUP_XML_ZIP" "$BACKUP_XML_FILE"*
	7z a "$BACKUP_XML_ZIP" "$BACKUP_XML_FILE"*

	echo rm -f "$BACKUP_XML_FILE"
	rm -f "$BACKUP_XML_FILE"

	# ADD this month's cache documents to the EXISTING 7zip file
	echo 7z a "$BACKUP_CACHE_ZIP" "$CACHE_DIR"/"$YEAR"/*_"$YEAR"-"$MONTH"-*
	7z a "$BACKUP_CACHE_ZIP" "$CACHE_DIR"/"$YEAR"/*_"$YEAR"-"$MONTH"-*

	#if tomorrow is the first day of the YEAR (01/01), then today is the last day of the year, so make this the final year backup by removing 'incremental' from filename
	if [ $(date --date='tomorrow' +%m) -eq 1 ]; then
		NEW_BACKUP_XML_ZIP=${BACKUP_XML_ZIP//-incremental/}
		mv $BACKUP_XML_ZIP $NEW_BACKUP_XML_ZIP
		BACKUP_XML_ZIP=$NEW_BACKUP_XML_ZIP

		NEW_BACKUP_CACHE_ZIP=${BACKUP_CACHE_ZIP//-incremental/}
		mv $BACKUP_CACHE_ZIP $NEW_BACKUP_CACHE_ZIP
		BACKUP_CACHE_ZIP=$NEW_BACKUP_CACHE_ZIP
	fi

	#upload to dropox (relies on dropbox uploader setup: https://github.com/andreafabrizi/Dropbox-Uploader which needs to be setup first)
	$SCRIPT_DIR/dropbox_uploader.sh upload "$BACKUP_XML_ZIP" $DROPBOX_XML_PATH
	$SCRIPT_DIR/dropbox_uploader.sh upload "$BACKUP_CACHE_ZIP" $DROPBOX_CACHE_PATH
	
	else 
		echo "Not last day of month: no backup performed"
fi
echo DONE
