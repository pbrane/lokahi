# Install


Create the project:
```
oc new-project stream
```

Install the chart:
```
helm upgrade -i opennms-local ./charts/opennms -f ./openshift-helm-values.yaml --namespace stream
```


# Debugging

```
oc login -u kubeadmin https://api.crc.testing:6443
oc new-project stream
```

```
helm upgrade -i operator-local ./charts/opennms-operator -f ./install-local-operator-values.yaml --namespace stream --create-namespace
```

```
helm upgrade -i opennms-local ./charts/opennms -f ./openshift-helm-values.yaml --namespace stream
```


stream.apps-crc.testing

```
Error: UPGRADE FAILED: failed to create resource: Internal error occurred: failed calling webhook "validate.nginx.ingress.kubernetes.io": failed to call webhook: Post "https://ingress-nginx-controller-admission.stream.svc:443/networking/v1/ingresses?timeout=10s": no endpoints available for service "ingress-nginx-controller-admission"
```

kubectl delete -A ValidatingWebhookConfiguration ingress-nginx-admission


# UI

```
cd ui
DOCKER_BUILDKIT=1 docker build -t opennms/horizon-stream-ui:local .

eval $(crc oc-env)
REGISTRY="$(oc get route/default-route -n openshift-image-registry -o=jsonpath='{.spec.host}')/openshift"
docker login -u kubeadmin -p $(oc whoami -t) $REGISTRY
docker tag opennms/horizon-stream-ui:local ${REGISTRY}/horizon-stream-ui:next
docker push ${REGISTRY}/horizon-stream-ui:next


docker tag opennms/horizon-stream-core:local ${REGISTRY}/horizon-stream-core:next
docker push ${REGISTRY}/horizon-stream-core:next

docker tag opennms/horizon-stream-grafana:local ${REGISTRY}/horizon-stream-grafana:next
docker push ${REGISTRY}/horizon-stream-grafana:next

docker tag opennms/horizon-stream-minion:local ${REGISTRY}/horizon-stream-minion:next
docker push ${REGISTRY}/horizon-stream-minion:next

docker tag opennms/horizon-stream-minion-gateway:local ${REGISTRY}/horizon-stream-minion-gateway:next
docker push ${REGISTRY}/horizon-stream-minion-gateway:next
```

# Minion gRPC


## Setup

openssl req -newkey rsa:4096 -nodes -keyout tls/tls.key -x509 -days 365 -out tls/tls.crt
oc create secret tls tls-secret --cert=tls/tls.crt --key=tls/tls.key

oc annotate ingresses.config/cluster ingress.operator.openshift.io/default-enable-http2=true

## Debugging

grpcurl -v -import-path shared-lib/horizon-ipc/ipc-grpc/ipc-grpc-contract/src/main/proto/ -proto opennms_minion_ipc.proto -insecure stream.apps-crc.testing:443 minion.CloudService.CloudToMinionMessages

grpcurl -v -import-path shared-lib/horizon-ipc/ipc-grpc/ipc-grpc-contract/src/main/proto/ -proto opennms_minion_ipc.proto -plaintext -d '{"system_id":"xx","location":"home"}' localhost:8990  minion.CloudService.CloudToMinionMessages

grpcurl -v -import-path shared-lib/horizon-ipc/ipc-grpc/ipc-grpc-contract/src/main/proto/ -proto opennms_minion_ipc.proto -d '{"system_id":"xx","location":"home"}' -insecure stream-minion.apps-crc.testing:443  minion.CloudService.CloudToMinionMessages

## Minion

docker run --rm -e MINION_ID=mine -e MINION_LOCATION=wagon -e GRPC_HOST=stream-minion.apps-crc.testing -e GRPC_PORT=443 -e GRPC_TLS_ENABLED=true -e GRPC_TLS_INSECURE=true opennms/horizon-stream-minion:local


# TODO

* Location dropdown broken
* Minion table does not show location
* Disable Ignite callbacks
   [ignite-update-notifier-timer] Your version is up to date.
* Allow Minions to run from outside of enviroment
* Fix /.m2/
* Ensure containers start w/o internet access (Maven access)
 * Seed containers, then pull
* Operator on OpenShift
