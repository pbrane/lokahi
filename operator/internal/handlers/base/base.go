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

package base

import (
	"github.com/OpenNMS-Cloud/opennms-operator/internal/handlers"
	"github.com/OpenNMS-Cloud/opennms-operator/internal/model/values"
	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
)

type BaseHandler struct {
	handlers.ServiceHandlerObject
}

func (h *BaseHandler) UpdateConfig(values values.TemplateValues) error {
	var namespace corev1.Namespace
	var certSecret corev1.Secret
	var endpointRole rbacv1.Role

	h.AddToTemplates(handlers.Filepath("_namespace.yaml"), values, &namespace)
	h.AddToTemplates(handlers.Filepath("cert/cert-secret.yaml"), values, &certSecret)
	h.AddToTemplates(handlers.Filepath("endpoints-role.yaml"), values, &endpointRole)

	return h.LoadTemplates()
}
