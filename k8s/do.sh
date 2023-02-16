#!/bin/sh
kubectl create -f https://download.elastic.co/downloads/eck/2.6.1/crds.yaml
kubectl apply -f https://download.elastic.co/downloads/eck/2.6.1/operator.yaml
kubectl apply -f elasticsearch-deployment.yaml
kubectl apply -f kibana-deployment.yaml

kubectl apply -f querier-deployment.yaml
kubectl apply -f querier-service.yaml
kubectl apply -f ingester-deployment.yaml
kubectl apply -f ingester-service.yaml

helm repo add eck-custom-resources https://xco-sk.github.io/eck-custom-resources/
helm install -f eck-custom-values.yaml eck-cr eck-custom-resources/eck-custom-resources-operator
kubectl apply -f elasticsearch-index.yaml

kubectl apply -f opennms-sc.yml
kubectl apply -f opennms-pv.yml
kubectl -n opennms-hs patch pvc elasticsearch-data-elasticsearch-es-default-0 -p '{"spec":{"storageClassName":"local-storage"}}'
