#!/bin/bash

trap stop EXIT INT

stop ()
{
	echo "Shutting down"
	kill $(jobs -p)
}

kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-core 11023:8101 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-core 11050:5005 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-core 11080:8181 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-minion 12022:8102 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-minion 12050:5005 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-minion 12080:8181 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-api 13050:5005 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-api 13080:9090 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/api-gateway 14080:80 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-notification 15050:5005 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-notification 15080:8080 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-horizon-stream-ui 17080:80 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/grafana 18080:3000 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/prometheus 19080:9090 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/prometheus-pushgateway 21080:9091 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/horizon-mail-server 22026:1025 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/horizon-mail-server 22080:8025 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-kafka 24090:59092 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-postgres 25054:5432 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-keycloak 26080:8080 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-minion 27022:8201 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-minion-gateway 16080:8080 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-minion-gateway 16050:5005 &
kubectl --context kind-kind port-forward --pod-running-timeout 1s --namespace default deployment/my-minion-gateway 16089:8990 &

while wait
do
	:
done
