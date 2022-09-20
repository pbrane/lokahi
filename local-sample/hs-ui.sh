#!/bin/bash

cd ../ui/

DOCKER_BUILDKIT=1

docker build -t opennms/horizon-stream-ui:local -f ./dev/Dockerfile . 2> ../local-sample/tmp/HS_UI.log

echo 1 > ../local-sample/tmp/HS_UI
