#!/bin/bash

# For in the cluster, not sure the CN, try the service in front of the pod. It
# is what the ingress will be forwarding to.
keytool -genkey -alias onms -keyalg RSA -keystore tmp/onms.keystore \
          -validity 3650 -storetype JKS \
          -dname "CN=opennms-rest-server, OU=Spring, O=Pivotal, L=Holualoa, ST=HI, C=US" \
          -keypass opennmspw -storepass opennmspw

kubectl -n local-instance create configmap rest-server.keystore --from-file=tmp/onms.keystore
kubectl -n local-instance create configmap rest-server.applications --from-file=../rest-server/src/main/resources/application.yml
