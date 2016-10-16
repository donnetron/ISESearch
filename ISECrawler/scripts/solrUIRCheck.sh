#!/bin/bash

#for i in {0000001..0100000}; do
#  curl http://debian-vm:8983/solr/select/?q=UID:$i\&rows=0\&wt=json >> UIDCheck1.log
#done

for i in {0100001..0200000}; do
  curl -s http://debian-vm:8983/solr/select/?q=UID:$i\&rows=0\&wt=json >> UIDCheck2.log
done

for i in {0200001..0300000}; do
  curl -s http://debian-vm:8983/solr/select/?q=UID:$i\&rows=0\&wt=json >> UIDCheck3.log
done

for i in {0300001..0370000}; do
  curl -s http://debian-vm:8983/solr/select/?q=UID:$i\&rows=0\&wt=json >> UIDCheck4.log
done

