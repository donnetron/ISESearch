#!/bin/bash

#absolute path to ISECrawler file
BASE_DIR=/home/don/ISESearch/ISECrawler
#URL to solr server
SOLR_URL=http://localhost:8983/solr/update

CLASSPATH=:lib/*:.
BACKUP_DIR="$BASE_DIR"/backup
CACHE_DIR="$BASE_DIR"/cache
DATA_DIR="$BASE_DIR"/data
POSTED_DIR="$BASE_DIR"/data/posted
LOG_DIR="$BASE_DIR"/logs
SCRIPT_DIR="$BASE_DIR"/scripts

DAY=$(date +%d)
MONTH=$(date +%m)
YEAR=$(date +%Y)

BACKUP_XML_FILE="$BACKUP_DIR"/"noticedata-$YEAR-incremental.xml"
BACKUP_XML_ZIP=${BACKUP_XML_FILE//.xml/.7z}

BACKUP_CACHE_ZIP="$BACKUP_DIR"/"cache-$YEAR-incremental.7z"

DROPBOX_XML_PATH=/
DROPBOX_CACHE_PATH=/