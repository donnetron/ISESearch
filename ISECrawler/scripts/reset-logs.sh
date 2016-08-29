#!/bin/bash
source var-config.sh

echo "--- Running reset-logs.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

rm -f $LOG_DIR/*.log
