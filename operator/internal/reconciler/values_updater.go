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
	"context"
	"github.com/OpenNMS/opennms-operator/api/v1alpha1"
	"github.com/OpenNMS/opennms-operator/internal/model/values"
	valuesutil "github.com/OpenNMS/opennms-operator/internal/util/crd"
	"github.com/OpenNMS/opennms-operator/internal/util/security"
	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/types"
)

// UpdateValues - update values for an instance based on it's crd
func (r *OpenNMSReconciler) UpdateValues(ctx context.Context, instance v1alpha1.OpenNMS) values.TemplateValues {
    if r.ValuesMap == nil {
        r.ValuesMap = map[string]values.TemplateValues{}
    }

    name := instance.Name
    namespace := instance.Spec.Namespace

    templateValues, ok := r.ValuesMap[name]
    if !ok {
        templateValues = r.DefaultValues
    }
    templateValues = valuesutil.ConvertCRDToValues(instance, templateValues)

    // only set new passwords if they weren't already created by a previous operator
    templateValues, existingCreds := r.CheckForExistingCoreCreds(ctx, templateValues, namespace)
    if !existingCreds { // only set new passwords if they weren't already created by a previous operator
        templateValues = setCorePasswords(templateValues, instance.Spec.Credentials)
    }

    templateValues, existingCreds = r.CheckForExistingPostgresCreds(ctx, templateValues, namespace)
    if !existingCreds {
        templateValues = setPostgresPassword(templateValues)
    }

    r.ValuesMap[name] = templateValues

    return templateValues
}

// CheckForExistingCoreCreds - checks if core credentials already exist for a given namespace
func (r *OpenNMSReconciler) CheckForExistingCoreCreds(ctx context.Context, v values.TemplateValues, namespace string) (values.TemplateValues, bool) {
	var credSecret v1.Secret
	err := r.Client.Get(ctx, types.NamespacedName{Namespace: namespace, Name: v.Values.Keycloak.ServiceName + "-initial-admin"}, &credSecret)
	if err != nil {
		return v, false
	}
	existingAdminPwd := string(credSecret.Data["password"])
	//existingUserPwd := string(credSecret.Data["userPwd"])
	//if existingAdminPwd == "" || existingUserPwd == "" {
	if existingAdminPwd == "" {
		return v, false
	}
	v.Values.Keycloak.AdminPassword = existingAdminPwd
	//v.Values.Keycloak.UserPassword = existingUserPwd
	return v, true
}

// CheckForExistingPostgresCreds - checks if core credentials already exist for a given namespace
func (r *OpenNMSReconciler) CheckForExistingPostgresCreds(ctx context.Context, v values.TemplateValues, namespace string) (values.TemplateValues, bool) {
    var credSecret v1.Secret
    err := r.Client.Get(ctx, types.NamespacedName{Namespace: namespace, Name: "postgres"}, &credSecret)
    if err != nil {
        return v, false
    }
    adminPwd := string(credSecret.Data["adminPwd"])
    keycloakPwd := string(credSecret.Data["keycloakPwd"])
    notificationPwd := string(credSecret.Data["notificationPwd"])
    grafanaPwd := string(credSecret.Data["grafanaPwd"])
    if adminPwd == "" || keycloakPwd == "" || notificationPwd == "" {
        return v, false
    }
    v.Values.Postgres.AdminPassword = adminPwd
    v.Values.Postgres.KeycloakPassword = keycloakPwd
    v.Values.Postgres.NotificationPassword = notificationPwd
    v.Values.Postgres.GrafanaPassword = grafanaPwd
    return v, true
}

// setCorePasswords - sets randomly generated passwords for the core if not already set
func setCorePasswords(tv values.TemplateValues, creds v1alpha1.Credentials) values.TemplateValues {
	if creds.AdminPassword == "" {
		tv.Values.Keycloak.AdminPassword = security.GeneratePassword(true)
	} else {
		tv.Values.Keycloak.AdminPassword = creds.AdminPassword
	}

	//if creds.UserPassword == "" {
	//	tv.Values.Keycloak.UserPassword = security.GeneratePassword(true)
	//} else {
	//	tv.Values.Keycloak.UserPassword = creds.UserPassword
	//}

	return tv
}

// setCorePasswords - sets randomly generated password for Postgres if not already set
func setPostgresPassword(tv values.TemplateValues) values.TemplateValues {
    tv.Values.Postgres.AdminPassword = security.GeneratePassword(true)
    tv.Values.Postgres.OpenNMSPassword = security.GeneratePassword(true)
    tv.Values.Postgres.KeycloakPassword = security.GeneratePassword(true)
    tv.Values.Postgres.NotificationPassword = security.GeneratePassword(true)

    //fed into Grafana via an .ini, so cannot generate with special characters
    tv.Values.Postgres.GrafanaPassword = security.GeneratePassword(false)
    return tv
}
