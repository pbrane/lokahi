#!/usr/bin/env bash

echo
echo ______________Creating Kind Cluster________________
echo
kind create cluster --config=../local-sample/config-kind.yaml
kubectl config use-context kind-kind
kubectl config get-contexts
