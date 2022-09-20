#!/bin/bash

cd ../rest-server/

mvn clean compile jib:dockerBuild -Dimage=opennms/horizon-stream-rest-server:local -DskipTests 1> ../local-sample/tmp/HS_REST_SERVER.log

echo 1 > ../local-sample/tmp/HS_REST_SERVER
