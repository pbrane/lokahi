#!/usr/bin/env bash

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

mkdir tmp

openssl genrsa -out tmp/ca.key
openssl req -new -x509 -days 365 -key tmp/ca.key -subj "/CN=onmshs/O=Test Keycloak./C=US" -out tmp/ca.crt
openssl req -newkey rsa:2048 -nodes -keyout tmp/server.key -subj "/CN=onmshs/O=Test Keycloak./C=US" -out tmp/server.csr
openssl x509 -req -extfile <(printf "subjectAltName=DNS:onmshs") -days 365 -in tmp/server.csr -CA tmp/ca.crt -CAkey tmp/ca.key -CAcreateserial -out tmp/server.crt
if [ $? -ne 0 ]; then exit; fi
kubectl -n local-instance create secret tls tls-cert-wildcard --cert tmp/server.crt --key tmp/server.key
if [ $? -ne 0 ]; then exit; fi
