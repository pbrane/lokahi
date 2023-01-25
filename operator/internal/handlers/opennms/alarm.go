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

package opennms

import (
	"github.com/OpenNMS-Cloud/opennms-operator/internal/handlers"
	"github.com/OpenNMS-Cloud/opennms-operator/internal/model/values"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
)

type AlarmHandler struct {
	handlers.ServiceHandlerObject
}

func (h *AlarmHandler) UpdateConfig(values values.TemplateValues) error {
	var alarmService corev1.Service
	var alarmDeployment appsv1.Deployment

	h.AddToTemplates(handlers.Filepath("opennms/alarm/alarm-service.yaml"), values, &alarmService)
	h.AddToTemplates(handlers.Filepath("opennms/alarm/alarm-deployment.yaml"), values, &alarmDeployment)

	return h.LoadTemplates()
}
