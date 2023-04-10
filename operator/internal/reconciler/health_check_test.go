//go:build unit
// +build unit

/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package reconciler

import (
	"errors"
	"github.com/OpenNMS-Cloud/opennms-operator/internal/model/values"
	"github.com/stretchr/testify/assert"
	"net/http"
	"sigs.k8s.io/controller-runtime/pkg/log/zap"
	"testing"
)

type mockHttp struct {
	response *http.Response
	err      error
}

func (h *mockHttp) Do(req *http.Request) (*http.Response, error) {
	return h.response, h.err
}

var httpClient = mockHttp{}
var r = OpenNMSReconciler{
	HttpClient: &httpClient,
	Log:        zap.New(),
}

func getTestInstance() *Instance {
	namespace := "mynamespace"
	svcName := "uiServer"
	port := 1234
	instance := &Instance{
		Name: "myinstance",
		Values: values.TemplateValues{
			Values: values.Values{
				OpenNMS: values.OpenNMSValues{
					UI: values.UIValues{
						ServiceValues: values.ServiceValues{
							ServiceName: svcName,
							Port:        port,
						},
					},
				},
			},
			Release: values.HelmRelease{
				Namespace: namespace,
			},
		},
	}
	return instance
}

func Test_instanceReady_ServiceStatus200OK_ReturnsTrue_NoError(t *testing.T) {
	instance := getTestInstance()
	httpClient.response = &http.Response{StatusCode: http.StatusOK}
	httpClient.err = nil
	res, err := r.instanceReady(instance)
	assert.Nil(t, err, "error should be nil")
	assert.True(t, res, "should return true when service returns 200")
}

func Test_instanceReady_ServiceStatus502BadGateway_ReturnsFalse_NoError(t *testing.T) {
	instance := getTestInstance()
	httpClient.response = &http.Response{StatusCode: http.StatusBadGateway}
	httpClient.err = nil
	res, err := r.instanceReady(instance)
	assert.Nil(t, err, "error should be nil")
	assert.False(t, res, "should return false when service doesn't return 200")
}

func Test_instanceReady_ClientError_ReturnsFalse_Error(t *testing.T) {
	instance := getTestInstance()
	httpClient.response = nil
	httpClient.err = errors.New("this is an error")
	res, err := r.instanceReady(instance)
	assert.NotNil(t, err, "error shouldn't be nil")
	assert.False(t, res, "should return false when there's an error")
}

func Test_instanceReady_RequestCreationError_ReturnsFalse_Error(t *testing.T) {
	instance := getTestInstance()
	instance.Values.Values.OpenNMS.UI.ServiceName = "thisistrash%%%%%%%%%%%%%%"
	res, err := r.instanceReady(instance)
	assert.NotNil(t, err, "error shouldn't be nil when a trash URL is created")
	assert.False(t, res, "should return false when there's an error")
}
