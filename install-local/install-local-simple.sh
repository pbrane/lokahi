#!/usr/bin/env bash

helm upgrade -i horizon-stream ./../charts/opennms -f ./fooker.yaml --namespace "opennms-hs" --create-namespace
