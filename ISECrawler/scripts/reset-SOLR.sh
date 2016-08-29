#!/bin/bash
source var-config.sh

echo "--- Running reset-SOLR.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

# delete all documents from the solr index
curl $SOLR_URL --data '<delete><query>*:*</query></delete>' -H 'Content-type:text/xml; charset=utf-8'
curl $SOLR_URL --data '<commit/>' -H 'Content-type:text/xml; charset=utf-8'

