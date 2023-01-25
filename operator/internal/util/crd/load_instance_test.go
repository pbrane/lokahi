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

package crd

import (
	"context"
	"github.com/OpenNMS-Cloud/opennms-operator/api/v1alpha1"
	"github.com/OpenNMS-Cloud/opennms-operator/internal/scheme"
	"github.com/stretchr/testify/assert"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
	"testing"
)

func TestGetInstance(t *testing.T) {
	cb := fake.ClientBuilder{}
	crd := &v1alpha1.OpenNMS{}
	crd.SetName("testdep")
	client := cb.WithScheme(scheme.GetScheme()).WithObjects(crd).Build()

	res, err := GetInstance(context.Background(), client, types.NamespacedName{Name: "testdep", Namespace: ""})

	assert.Nil(t, err)
	assert.Equal(t, crd.GetName(), res.GetName(), "should return the expected crd")

	res, err = GetInstance(context.Background(), client, types.NamespacedName{Name: "!exists", Namespace: ""})

	assert.NotNil(t, err, "should return an error when the CRD can't be found")
}
