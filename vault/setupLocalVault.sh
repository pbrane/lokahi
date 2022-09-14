#! /bin/sh
set -x


runVault()
{
  which vault > /dev/null 2>&1
  if [ $? -eq 1 ]; then
    echo "Must install vault locally first (e.g. apt install vault)"
    exit 1
  fi
  nohup vault server -dev -dev-root-token-id root -dev-listen-address 0.0.0.0:8200 &
  sleep 10
}

configureVault()
{
  export VAULT_ADDR=http://0.0.0.0:8200
  vault login root

  vault secrets enable -path=internal kv-v2

  # For kubernetes <=1.23 (creates token automatically): 
  export token=`kubectl describe serviceaccount vault | grep "Mountable secrets" | awk '{print $3}'`

  # For kubernetes 1.24+, need to create token ourselves:
  # export token='vault-token-g955r'
  # kubectl create secret service-account-token "$token" 
  # kubectl annotate secret "$token" "kubernetes.io/service-account.name"="vault"

  vault auth enable kubernetes
  export JWT=$(kubectl get secret $token --output='go-template={{ .data.token }}' | base64 --decode)
  export CA_CERT="$(kubectl config view --raw --minify --flatten --output='jsonpath={.clusters[].cluster.certificate-authority-data}' | base64 --decode)"
  export KUBE_HOST="$(kubectl config view --raw --minify --flatten --output='jsonpath={.clusters[].cluster.server}')"

  vault write auth/kubernetes/config \
      token_reviewer_jwt="$JWT" \
      kubernetes_host="$KUBE_HOST" \
      kubernetes_ca_cert="$CA_CERT" \
      issuer="https://kubernetes.default.svc.cluster.local"

  vault policy write stream-trusted - <<EOF
path "internal/data/database/config" {
  capabilities = ["read"]
}
EOF
  
  vault write auth/kubernetes/role/stream-trusted \
bound_service_account_names=stream-trusted \
bound_service_account_namespaces=default \
  policies=stream-trusted \
  ttl=24h

  vault kv put internal/database/config username=postgres password=passw0rd zookeeperuser=zookeeper-user zookeeperpassword=passw0rd kafkauser=kafka-user kafkapassword=passw0rd
}

installVaultAgentInjectorOnly()
{
  # If using an external vault instead of deploying to the cluster (only
  # installs the agent injector. Must create the external-vault service and
  # endpoint for this to work:
  helm install vault https://github.com/hashicorp/vault-helm/archive/refs/tags/v0.22.0.tar.gz --set "injector.externalVaultAddr=http://external-vault:8200"
  kubectl apply -f external-vault.yaml
}

installVaultAgentInjectorOnly
runVault
configureVault

echo "Ensure that the skaffold kubernetes deployments are modified to include the VAULT_ADDR and VAULT_TOKEN, otherwise they will not be able to communicate with the external vault.

