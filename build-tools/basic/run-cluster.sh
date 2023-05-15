#!/bin/sh

./install-local/load-or-generate-secret.sh "default" "root-ca-certificate"  "opennms-ca" "target/tmp/server-ca.key" "target/tmp/server-ca.crt"
./install-local/generate-and-sign-certificate.sh "default" "opennms-minion-gateway-certificate" "minion.onmshs.local" "target/tmp/server-ca.key" "target/tmp/server-ca.crt"
./install-local/generate-and-sign-certificate.sh "default" "opennms-ui-certificate" "onmshs.local" "target/tmp/server-ca.key" "target/tmp/server-ca.crt"
./install-local/load-or-generate-secret.sh "default" "client-root-ca-certificate" "client-ca" "target/tmp/client-ca.key" "target/tmp/client-ca.crt"
helm upgrade -i opennms ./charts/opennms -f tilt-helm-values.yaml \
  --set Grafana.Image=opennms/horizon-stream-grafana:local-basic \
  --set Keycloak.Image=opennms/horizon-stream-keycloak:local-basic \
  --set OpenNMS.API.Image=opennms/horizon-stream-rest-server:local-basic \
  --set OpenNMS.Alert.Image=opennms/horizon-stream-alert:local-basic \
  --set OpenNMS.DataChoices.Image=opennms/horizon-stream-datachoices:local-basic \
  --set OpenNMS.Events.Image=opennms/horizon-stream-events:local-basic \
  --set OpenNMS.Inventory.Image=opennms/horizon-stream-inventory:local-basic \
  --set OpenNMS.MetricsProcessor.Image=opennms/horizon-stream-metrics-processor:local-basic \
  --set OpenNMS.Minion.Image=opennms/horizon-stream-minion:local-basic \
  --set OpenNMS.MinionGateway.Image=opennms/horizon-stream-minion-gateway:local-basic \
  --set OpenNMS.Notification.Image=opennms/horizon-stream-notification:local-basic \
  --set OpenNMS.MinionCertificateManager.Image=opennms/horizon-stream-minion-certificate-manager:local-basic \
  --set OpenNMS.MinionCertificateVerifier.Image=opennms/horizon-stream-minion-certificate-verifier:local-basic \
  --set OpenNMS.UI.Image=opennms/horizon-stream-ui:local-basic \
