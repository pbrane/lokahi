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
	rbacv1 "k8s.io/api/rbac/v1"
)

func GetOpenNMSHandlers() []handlers.ServiceHandler {
	return []handlers.ServiceHandler{
		&ApiHandler{},
		&UIHandler{},
		&MinionHandler{},
		&InventoryHandler{},
		&NotificationsHandler{},
		&MetricsProcessorHandler{},
		&EventsHandler{},
		&AlarmHandler{},
		&DataChoicesHandler{},
	}
}

type OpenNMSHandler struct {
	handlers.ServiceHandlerObject
}

func (h *OpenNMSHandler) UpdateConfig(values values.TemplateValues) error {
	//api
	var apiService corev1.Service
	var apiDeployment appsv1.Deployment

	h.AddToTemplates(handlers.Filepath("opennms/api/api-service.yaml"), values, &apiService)
	h.AddToTemplates(handlers.Filepath("opennms/api/api-deployment.yaml"), values, &apiDeployment)

	//ui
	var uiDeployment appsv1.Deployment
	var uiService corev1.Service

	h.AddToTemplates(handlers.Filepath("opennms/ui/ui-deployment.yaml"), values, &uiDeployment)
	h.AddToTemplates(handlers.Filepath("opennms/ui/ui-service.yaml"), values, &uiService)

	//minion
	var minionCM corev1.ConfigMap
	var minionSA corev1.ServiceAccount
	var minionRB rbacv1.RoleBinding
	var minionSVC corev1.Service
	var minionGatewaySVC corev1.Service
	var minionGatewayIgniteSVC corev1.Service
	var minionDeploy appsv1.Deployment
	var minionGatewayDeploy appsv1.Deployment
	var minionGGPSA corev1.ServiceAccount
	var minionGGPSVC corev1.Service
	var minionGGPDeploy appsv1.Deployment

	h.AddToTemplates(handlers.Filepath("opennms/minion/minion-configmap.yaml"), values, &minionCM)
	h.AddToTemplates(handlers.Filepath("opennms/minion/minion-serviceaccount.yaml"), values, &minionSA)
	h.AddToTemplates(handlers.Filepath("opennms/minion/minion-rolebinding.yaml"), values, &minionRB)
	h.AddToTemplates(handlers.Filepath("opennms/minion/minion-service.yaml"), values, &minionSVC)
	h.AddToTemplates(handlers.Filepath("opennms/minion/minion-gateway-service.yaml"), values, &minionGatewaySVC)
	h.AddToTemplates(handlers.Filepath("opennms/minion/minion-gateway-ignite-service.yaml"), values, &minionGatewayIgniteSVC)
	h.AddToTemplates(handlers.Filepath("opennms/minion/minion-deployment.yaml"), values, &minionDeploy)
	h.AddToTemplates(handlers.Filepath("opennms/minion/minion-gateway-deployment.yaml"), values, &minionGatewayDeploy)
	h.AddToTemplates(handlers.Filepath("opennms/minion-gateway-grpc-proxy/minion-gateway-grpc-proxy-serviceaccount.yaml"), values, &minionGGPSA)
	h.AddToTemplates(handlers.Filepath("opennms/minion-gateway-grpc-proxy/minion-gateway-grpc-proxy-service.yaml"), values, &minionGGPSVC)
	h.AddToTemplates(handlers.Filepath("opennms/minion-gateway-grpc-proxy/minion-gateway-grpc-proxy-deployment.yaml"), values, &minionGGPDeploy)

	//inventory
	var inventoryDeployment appsv1.Deployment
	var inventoryService corev1.Service

	h.AddToTemplates(handlers.Filepath("opennms/inventory/inventory-deployment.yaml"), values, &inventoryDeployment)
	h.AddToTemplates(handlers.Filepath("opennms/inventory/inventory-service.yaml"), values, &inventoryService)

	//notification
	var noteDeployment appsv1.Deployment
	var noteService corev1.Service

	h.AddToTemplates(handlers.Filepath("opennms/notification/notification-deployment.yaml"), values, &noteDeployment)
	h.AddToTemplates(handlers.Filepath("opennms/notification/notification-service.yaml"), values, &noteService)

	//metrics processor
	var mpDeployment appsv1.Deployment

	h.AddToTemplates(handlers.Filepath("opennms/metricsprocessor/metricsprocessor-deployment.yaml"), values, &mpDeployment)

	//Events
	var eventsDeployment appsv1.Deployment
	var eventsService corev1.Service

	h.AddToTemplates(handlers.Filepath("opennms/events/events-deployment.yaml"), values, &eventsDeployment)
	h.AddToTemplates(handlers.Filepath("opennms/events/events-deployment.yaml"), values, &eventsDeployment)
	h.AddToTemplates(handlers.Filepath("opennms/events/events-service.yaml"), values, &eventsService)

	return h.LoadTemplates()
}
