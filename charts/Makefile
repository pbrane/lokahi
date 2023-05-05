.PHONY: init-helm package

all: package

init-helm:
	helm repo add jetstack https://charts.jetstack.io
	helm dependency build opennms

package:
	helm package opennms -d ./packaged
	helm repo index --url https://opennms-cloud.github.io/horizon-stream/charts/packaged --merge packaged/index.yaml ./packaged
