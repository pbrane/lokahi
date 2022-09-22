mkdir -p build-tools/basic/run

# Update the manifest to use our image tag
cat dev/kubernetes.kafka.yaml | \
	sed -e 's|image: opennms/grafana-dev\s*$|image: opennms/grafana-dev:local-basic|' \
	    -e 's|image: opennms/horizon-stream-keycloak-dev\s*$|image: opennms/horizon-stream-keycloak-dev:local-basic|' \
	    -e 's|image: opennms/horizon-stream-core\s*$|image: opennms/horizon-stream-core:local-basic|' \
	    -e 's|image: opennms/horizon-stream-minion\s*$|image: opennms/horizon-stream-minion:local-basic|' \
	    -e 's|image: opennms/horizon-stream-api\s*$|image: opennms/horizon-stream-api:local-basic|' \
	    -e 's|image: opennms/horizon-stream-notification\s*$|image: opennms/horizon-stream-notification:local-basic|' \
	    -e 's|image: opennms/horizon-stream-minion-gateway\s*$|image: opennms/horizon-stream-minion-gateway:local-basic|' \
	    -e 's|image: opennms/horizon-stream-ui\s*$|image: opennms/horizon-stream-ui:local-basic|' \
	> build-tools/basic/run/local-dev.manifest.yaml
