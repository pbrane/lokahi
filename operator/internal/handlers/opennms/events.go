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

type EventsHandler struct {
	handlers.ServiceHandlerObject
}

func (h *EventsHandler) UpdateConfig(values values.TemplateValues) error {
	var eventsDeployment appsv1.Deployment
	var eventsService corev1.Service

	h.AddToTemplates(handlers.Filepath("opennms/events/events-deployment.yaml"), values, &eventsDeployment)
	h.AddToTemplates(handlers.Filepath("opennms/events/events-deployment.yaml"), values, &eventsDeployment)
	h.AddToTemplates(handlers.Filepath("opennms/events/events-service.yaml"), values, &eventsService)

	return h.LoadTemplates()
}
