#!/bin/bash

# Create the manifest if we don't have one from the last run of run-cluster.sh
if [ ! -f build-tools/basic/run/local-dev.manifest.yaml ]
then
	./build-tools/basic/prepare-manifest.sh
fi

kubectl delete -f build-tools/basic/run/local-dev.manifest.yaml
kubectl delete -f https://raw.githubusercontent.com/keycloak/keycloak-k8s-resources/18.0.0/kubernetes/kubernetes.yml
kubectl delete -f https://raw.githubusercontent.com/keycloak/keycloak-k8s-resources/18.0.0/kubernetes/keycloakrealmimports.k8s.keycloak.org-v1.yml
kubectl delete -f https://raw.githubusercontent.com/keycloak/keycloak-k8s-resources/18.0.0/kubernetes/keycloaks.k8s.keycloak.org-v1.yml
