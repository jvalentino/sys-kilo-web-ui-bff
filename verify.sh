#!/bin/sh
HELM_NAME=sys-web-ui-bff

kubectl port-forward --namespace default svc/$HELM_NAME 8082:8080 > build/$HELM_NAME.log 2>&1 &
curl http://localhost:8082/actuator/health

while [ $? -ne 0 ]; do
    kubectl port-forward --namespace default svc/$HELM_NAME 8082:8080 > build/$HELM_NAME.log 2>&1 &
    curl http://localhost:8082/actuator/health
    sleep 5
done
