IMAGE ?= opennms/operator:local-build

.PHONY: dependencies build alpine-build unit-test integration-test
.PHONY: local-docker kind-load copy-charts kubebuilder

all: build

unit-test:
	go test --tags=unit ./...

integration-test:
	echo "These are integration tests"

kubebuilder:
	controller-gen object paths=./api/v1alpha1/opennms_types.go

crd:
	controller-gen crd paths=./api/v1alpha1/opennms_types.go output:crd:artifacts:config=./api/crd/bases
	mv ./api/crd/bases/k8s.opennms.com_opennms.yaml ../charts/opennms-operator/crds/opennms.yaml

dependencies:
	go mod download

copy-charts:
	rm -rf charts/
	cp -rf ../charts .

local-docker: copy-charts
	docker build -t $(IMAGE) .

kind-load: local-docker
	kind load docker-image $(IMAGE)

build:
	go build -a -o operator cmd/opennms-operator/main.go

alpine-build:
	CGO_ENABLED=0 GOOS=linux GOARCH=amd64 GO111MODULE=on go build -a -o operator cmd/opennms-operator/main.go
