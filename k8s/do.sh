#!/bin/sh
kubectl create -f https://download.elastic.co/downloads/eck/2.6.1/crds.yaml
kubectl apply -f https://download.elastic.co/downloads/eck/2.6.1/operator.yaml
kubectl apply -f elasticsearch-deployment.yaml
kubectl apply -f querier-deployment.yaml
kubectl apply -f querier-service.yaml
kubectl apply -f ingester-deployment.yaml
kubectl apply -f ingester-service.yaml
