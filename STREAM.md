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
```

# TODO

* Fix /.m2/
* Ensure containers start w/o internet access (Maven access)
 * Seed containers, then pull
* Operator on OpenShift

