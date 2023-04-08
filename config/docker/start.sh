#!/bin/bash
cd /opt/fluent-bit/bin
./fluent-bit -c fluentbit.conf > fluentbit.log 2>&1 &

cd /usr/local
java -jar \
  -Dspring.redis.host=redis-master \
  -Dmanagement.apiDocUrl=http://sys-rest-doc:8080 \
  -Dmanagement.apiUserUrl=http://sys-rest-user:8080 \
  sys-kilo-web-ui-bff-0.0.1.jar
