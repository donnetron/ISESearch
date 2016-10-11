#!/bin/bash
source /home/don/ISESearch/ISECrawler/scripts/var-config.sh

echo "--- Running statistics.sh ---"
#echo "--- SCRIPTS MUST BE RUN FROM WITHIN SCRIPTS DIRECTORY TO INCLUDE var-config.sh ---"

cd $BASE_DIR
pwd
echo "{\"statistics\":" > cache/statistics.json

#total number notices published (as "matches"). Also total number of unique companies (as "ngroups")
echo "{\"totalNotices\":" 2>&1 | tee -a cache/statistics.json
#curl "http://localhost:8983/solr/select?q=*:*&group=true&group.field=company_str&group.ngroups=true&rows=0&wt=json" 2>&1 | tee cache/statistics.json
curl -s "$SOLR_URL/select?q=*:*&group=true&group.field=company_str&group.ngroups=true&rows=0&wt=json" 2>&1 | tee -a cache/statistics.json
echo "," 2>&1 | tee -a cache/statistics.json

#top 20 companies with most notices published
echo "\"companyNotices\":" 2>&1 | tee -a cache/statistics.json
curl -s "$SOLR_URL/select?q=*:*&facet=true&facet.field=company_str&facet.sort=count&facet.limit=15&rows=0&wt=json" 2>&1 | tee -a cache/statistics.json
echo "," 2>&1 | tee -a cache/statistics.json

#number of notices published in each hour during the day (ordered sequentially by hour)
echo "\"hourNotices\":" 2>&1 | tee -a cache/statistics.json
curl -s "$SOLR_URL/select?q=*:*&facet=true&facet.field=hour&facet.sort=hour+asc&rows=0&wt=json" 2>&1 | tee -a cache/statistics.json
echo "," 2>&1 | tee -a cache/statistics.json

#UGLY SEARCH (big result - could potentically be reduced by facet_count in later versions of Solr)
#number of notices published per day
echo "\"dayNotices\":" 2>&1 | tee -a cache/statistics.json
curl -s "$SOLR_URL/select?q=datetime:*&facet=true&facet.date=datetime&facet.date.start=2003-01-01T00:00:00.100Z&facet.date.end=NOW/MONTH&facet.date.gap=%2B1DAY&rows=0&wt=json" 2>&1 | tee -a cache/statistics.json
echo "," 2>&1 | tee -a cache/statistics.json

#number of notices published each month (big array, sequentially from beginning incrementing by month)
echo "\"monthNotices\":" 2>&1 | tee -a cache/statistics.json
curl -s "$SOLR_URL/select?q=*:*&facet=true&facet.date=datetime&facet.date.start=2003-01-01T00:00:00.100Z&facet.date.end=NOW/MONTH&facet.date.gap=%2B1MONTH&rows=0&wt=json" 2>&1 | tee -a cache/statistics.json
echo "," 2>&1 | tee -a cache/statistics.json

#number of notices published in a year
echo "\"yearNotices\":" 2>&1 | tee -a cache/statistics.json
curl -s "$SOLR_URL/select?q=*:*&facet=true&facet.date=datetime&facet.date.start=2003-01-01T00:00:00.100Z&facet.date.end=NOW/YEAR%2B1YEAR&facet.date.gap=%2B1YEAR&rows=0&wt=json" 2>&1 | tee -a cache/statistics.json
echo "," 2>&1 | tee -a cache/statistics.json

#total notices (and last 3 companies by date) publishing a notice containing "2003/12/EC" 
echo "\"directive12Notices\":" 2>&1 | tee -a cache/statistics.json
curl -s "$SOLR_URL/select?q=\"2003/12/EC\"&rows=3&sort=datetime%20desc&fl=company%20url%20datetime&wt=json" 2>&1 | tee -a cache/statistics.json
echo "}" 2>&1 | tee -a cache/statistics.json

echo "}" 2>&1 | tee -a cache/statistics.json

echo DONE