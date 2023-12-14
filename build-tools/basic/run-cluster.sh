#!/bin/sh

helm upgrade -i lokahi ./charts/lokahi -f build-tools/basic/helm-values.yaml \
  --set global.image.tag=local-basic
