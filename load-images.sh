#!/bin/bash

tag=${1:-local}

images=(
  "opennms/horizon-stream-ui:$tag"
  "opennms/horizon-stream-core:$tag"
  "opennms/horizon-stream-minion:$tag"
  "opennms/horizon-stream-minion-gateway:$tag"
  "opennms/horizon-stream-api:$tag"
  "opennms/horizon-stream-notification:$tag"
  "opennms/horizon-stream-keycloak-dev:$tag"
  "opennms/grafana-dev:$tag"
)

for image in ${images[*]}; do
    kind load docker-image $image
done
