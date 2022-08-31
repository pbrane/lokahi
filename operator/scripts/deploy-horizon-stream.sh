#!/usr/bin/env bash
#use this script to install a basic version of OpenNMS Horizon Stream locally

bash scripts/create-kind-cluster.sh

bash scripts/install-operator-local.sh

bash scripts/create-instance.sh

bash scripts/add-local-ssl-cert.sh

kubectl config set-context --current --namespace=local-instance

kubectl get pods -w
