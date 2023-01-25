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
	"github.com/OpenNMS-Cloud/opennms-operator/internal/handlers/base"
	"github.com/OpenNMS-Cloud/opennms-operator/internal/handlers/ingress"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestInitServiceHandlers(t *testing.T) {
	r := OpenNMSReconciler{}
	r.InitServiceHandlers()
	length := len(r.StandardHandlers)
	first := r.StandardHandlers[0]
	last := r.StandardHandlers[length-1]

	assert.IsType(t, &base.BaseHandler{}, first, "First handler must be the base handler")
	assert.IsType(t, &ingress.IngressHandler{}, last, "Last handler must be the ingress handler")
}
