#!/bin/bash

#use this script to install a basic version of OpenNMS Horizon Stream locally

echo
echo ______________Creating Kind Cluster________________
echo

kind create cluster --config=./install-local-config-kind.yaml
kubectl config use-context kind-kind
kubectl config get-contexts

echo __________________Installing OLM___________________
echo
operator-sdk olm install --version v0.21.2

echo
echo ___________Installing Helm Dependencies____________
echo
helm upgrade -i operator-deps-local ./charts/opennms-operator-dependencies -f ./operator/values.yaml
if [ $? -ne 0 ]; then exit; fi

echo
echo _______________Wait For Dependencies_______________
echo
until kubectl wait -n kafka --for=condition=Ready=true pod -l name=strimzi-cluster-operator --timeout=90s 2> /dev/null
do
    sleep 5
    echo Waiting for dependencies to start....
done
if [ $? -ne 0 ]; then exit; fi
echo Dependencies started.

echo
echo ________________Installing Operator________________
echo
helm upgrade -i operator-local ./charts/opennms-operator -f ./install-local-operator-values.yaml --namespace opennms --create-namespace
if [ $? -ne 0 ]; then exit; fi

echo
echo ____________Installing Local Instance______________
echo
kubectl apply -f ./install-local-onms-instance.yaml
if [ $? -ne 0 ]; then exit; fi

echo
echo ____________Waiting for Local Instance_____________
echo

until kubectl wait --for=jsonpath='{.status.phase}'=Active namespace/local-instance --timeout=30s 2> /dev/null
do
    sleep 2
    echo Waiting for local-instance namespace....
done
if [ $? -ne 0 ]; then exit; fi
echo local-instance namespace found


echo
echo _________Creating and Applying SSL Cert____________
echo

mkdir -p tmp

openssl genrsa -out tmp/ca.key
openssl req -new -x509 -days 365 -key tmp/ca.key -subj "/CN=onmshs/O=Test Keycloak./C=US" -out tmp/ca.crt
openssl req -newkey rsa:2048 -nodes -keyout tmp/server.key -subj "/CN=onmshs/O=Test Keycloak./C=US" -out tmp/server.csr
openssl x509 -req -extfile <(printf "subjectAltName=DNS:onmshs") -days 365 -in tmp/server.csr -CA tmp/ca.crt -CAkey tmp/ca.key -CAcreateserial -out tmp/server.crt
if [ $? -ne 0 ]; then exit; fi
kubectl -n local-instance create secret tls tls-cert-wildcard --cert tmp/server.crt --key tmp/server.key
if [ $? -ne 0 ]; then exit; fi

kubectl config set-context --current --namespace=local-instance

kubectl get pods -w
