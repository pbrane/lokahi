#! /bin/sh
set -x

helm install vault https://github.com/hashicorp/vault-helm/archive/refs/tags/v0.22.0.tar.gz --set "server.dev.enabled=true"

kubectl get po vault-0 > /dev/null 2>&1
until [ $? -eq 0 ]
do
  sleep 2
  kubectl get po vault-0 > /dev/null 2>&1
done
kubectl wait --for=condition=ready pod/vault-0

kubectl exec -it vault-0 -- vault secrets enable -path=internal kv-v2

kubectl exec -it vault-0 -- vault auth enable kubernetes
kubectl exec -it vault-0 -- sh -c 'vault write auth/kubernetes/config kubernetes_host="https://$KUBERNETES_PORT_443_TCP_ADDR:443"'


kubectl exec -it vault-0 -- vault kv put internal/database/config username=postgres password=passw0rd zookeeperuser=zookeeper-user zookeeperpassword=passw0rd kafkauser=kafka-user kafkapassword=passw0rd
# kubectl exec -it vault-0 -- vault kv put internal/database/zoo zookeeperuser=zookeper-user zookeeperpassword=passw0rd
# kubectl exec -it vault-0 -- vault kv put internal/kafka/config kafkauser=kafka-user kafkapassword=passw0rd
kubectl exec -it vault-0 -- sh -c 'vault policy write stream-trusted - <<EOF
path "internal/data/database/config" {
  capabilities = ["read"]
}
EOF'
kubectl exec -it vault-0 -- sh -c 'vault write auth/kubernetes/role/stream-trusted \
  bound_service_account_names=stream-trusted \
  bound_service_account_namespaces=default \
    policies=stream-trusted \
    ttl=24h'





