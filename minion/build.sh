#!/bin/bash
mvn clean install -Pbuild-docker-images-enabled -DskipTests -Ddocker.image=opennms/horizon-stream-minion:local -Ddocker.skipPush=true

eval $(crc oc-env)
REGISTRY="$(oc get route/default-route -n openshift-image-registry -o=jsonpath='{.spec.host}')/openshift"
docker tag opennms/horizon-stream-minion:local ${REGISTRY}/horizon-stream-minion:next
docker push ${REGISTRY}/horizon-stream-minion:next
