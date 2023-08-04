#!/usr/bin/env bash

# https://github.com/olivergondza/bash-strict-mode
set -eEuo pipefail
trap 's=$?; echo >&2 "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $s' ERR

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"/..

function die() {
  echo "Error: $1" >&2
  exit 1
}

if [ $# -ge 2 ] && [ "$1" == "-t" ]; then
  shift
  tries="$1"; shift
else
  tries=1
fi

if [ $# -lt 2 ]; then
    echo "$(basename "$0"): too few command-line arguments" >&2
    echo "usage: $(basename "$0"): [-t <tries>] <domain> <port> [<kubectl options>]" >&2
    echo "example: $(basename "$0"): onmshs.local 1443" >&2
    exit 1
fi

domain="$1"; shift
port="$1"; shift

mkdir -p $DIR/target

kubectl get secret client-root-ca-certificate 2>&1 > /dev/null || die "Kubernetes secret with 'Client CA' secret not found"
echo "'Client CA' secret found"

kubectl get secret client-root-ca-certificate -ogo-template='{{index .data "tls.crt" }}' | base64 --decode > $DIR/target/client-ca.crt || die "'Client CA' certificate not found"
kubectl get secret client-root-ca-certificate -ogo-template='{{index .data "tls.key" }}' | base64 --decode > $DIR/target/client-ca.key || die "'Client CA' private key not found"
echo "'Client CA' certificate and private key extracted fine"

kubectl get secret root-ca-certificate 2>&1 > /dev/null || die "Kubernetes secret with 'Server CA' secret not found"
echo "'Server CA' secret found"
kubectl get secret root-ca-certificate -ogo-template='{{index .data "tls.crt" }}' | base64 --decode > $DIR/target/server-ca.crt || die "'Server CA' certificate not found"
kubectl get secret root-ca-certificate -ogo-template='{{index .data "tls.key" }}' | base64 --decode > $DIR/target/server-ca.key || die "'Server CA' private key not found"
echo "'Server CA' certificate and private key extracted fine"

openssl verify -CAfile $DIR/target/server-ca.crt $DIR/target/server-ca.crt || die "'Server CA' certificate could not be verified"

kubectl get secret opennms-ui-certificate 2>&1 > /dev/null || die "Kubernetes secret 'opennms-ui-certificate' not found"
echo "'UI Ingress' secret found"
kubectl get secret opennms-ui-certificate -ogo-template='{{index .data "tls.crt" }}' | base64 --decode > $DIR/target/ui.crt || die "'opennms-ui-certificate' certificate not found"
echo "'UI Ingress'  certificate and private key extracted fine"

openssl verify -CAfile $DIR/target/server-ca.crt $DIR/target/ui.crt || die "'UI Ingress' certificate could not be verified"

kubectl get secret opennms-minion-gateway-certificate 2>&1 > /dev/null || die "Kubernetes secret 'opennms-minion-gateway-certificate' not found"
echo "'Minion Gateway Ingress' secret found"
kubectl get secret opennms-minion-gateway-certificate -ogo-template='{{index .data "tls.crt" }}' | base64 --decode > $DIR/target/mgw.crt || die "'opennms-minion-gateway-certificate' certificate not found"
echo "'Minion Gateway' certificate and private key extracted fine"

openssl verify -CAfile $DIR/target/server-ca.crt $DIR/target/mgw.crt || die "'Minion Gateway' certificate could not be verified"

while true; do
  if curl -sSf --cacert $DIR/target/server-ca.crt --resolve "${domain}:${port}:127.0.0.1" https://${domain}:${port}/ 2>&1 > /dev/null; then
    break
  fi
  tries=$(expr $tries - 1)
  if [ $tries -le 0 ]; then
    die "'UI Ingress' failed to verify HTTPS connection using extracted CA certificate"
  else
    echo "'UI Ingress' failed to verify HTTPS connection using extracted CA certificate -- will try again in 1 second ($tries tries left)" >&2
  fi
  sleep 1
done

# This doesn't work unless we provide a client certificate
#curl -sSf --cacert $DIR/target/server-ca.crt --resolve "minion.${domain}:${port}:127.0.0.1" https://minion.${domain}:${port}/ 2>&1 > /dev/null || die "'Minion Gateway' failed to verify HTTPS connection using extracted CA certificate"

# "openssl s_client" will get poll error and return non-zero, so we
# temporarily disable pipefail here.
set +o pipefail
openssl s_client -CAfile target/tmp/server-ca.crt -connect minion.${domain}:${port} -servername minion.${domain} < /dev/null | openssl verify -CAfile target/tmp/server-ca.crt /dev/stdin || die "'Minion Gateway' failed to verify HTTPS connection using extracted CA certificate"
set -o pipefail

echo "============"
echo "= ALL GOOD ="
echo "============"
