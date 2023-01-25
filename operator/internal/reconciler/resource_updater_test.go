//go:build unit

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
    "github.com/OpenNMS-Cloud/opennms-operator/api/v1alpha1"
    "github.com/OpenNMS-Cloud/opennms-operator/internal/scheme"
    "github.com/stretchr/testify/assert"
    appsv1 "k8s.io/api/apps/v1"
    batchv1 "k8s.io/api/batch/v1"
    corev1 "k8s.io/api/core/v1"
    "sigs.k8s.io/controller-runtime/pkg/client/fake"
    "testing"
    "time"
)

func TestUpdateDeployment(t *testing.T) {
    instance := v1alpha1.OpenNMS{}
    instance.SetName("instance")
    client := fake.NewClientBuilder().WithScheme(scheme.GetScheme()).WithObjects(&instance).Build()
    r := OpenNMSReconciler{Client: client}
    ctx := context.Background()

    deployed := appsv1.Deployment{}
    deployed.SetName("name")
    desired := appsv1.Deployment{}
    desired.SetName("name")

    res, err := r.updateDeployment(ctx, &instance, &desired, &deployed)
    assert.Nil(t, res)
    assert.Nil(t, err, "noop if desired and deployed are the same")

    desired.Spec.Paused = true

    res, err = r.updateDeployment(ctx, &instance, &desired, &deployed)
    assert.NotNil(t, err, "should error if resource being updated doesn't exist")

    err = client.Create(ctx, &deployed)
    assert.Nil(t, err)

    res, err = r.updateDeployment(ctx, &instance, &desired, &deployed)
    assert.Nil(t, err)
    assert.NotNil(t, res)
    assert.Equal(t, 10*time.Second, res.RequeueAfter, "should update and then requeue")

    deployed.Status.ReadyReplicas = 0
    deployed.Status.Replicas = 2
    deployed.Spec.Paused = true

    res, err = r.updateDeployment(ctx, &instance, &desired, &deployed)
    assert.Nil(t, err)
    assert.NotNil(t, res)
    assert.Equal(t, 15*time.Second, res.RequeueAfter, "should requeue to wait for replicas")
}

func TestUpdateStatefulSet(t *testing.T) {
    instance := v1alpha1.OpenNMS{}
    instance.SetName("instance")
    client := fake.NewClientBuilder().WithScheme(scheme.GetScheme()).WithObjects(&instance).Build()
    r := OpenNMSReconciler{Client: client}
    ctx := context.Background()

    deployed := appsv1.StatefulSet{}
    deployed.SetName("name")
    desired := appsv1.StatefulSet{}
    desired.SetName("name")

    res, err := r.updateStatefulSet(ctx, &instance, &desired, &deployed)
    assert.Nil(t, res)
    assert.Nil(t, err, "noop if desired and deployed are the same")

    desired.Spec.ServiceName = "service"

    res, err = r.updateStatefulSet(ctx, &instance, &desired, &deployed)
    assert.NotNil(t, err, "should error if resource being updated doesn't exist")

    err = client.Create(ctx, &deployed)
    assert.Nil(t, err)

    res, err = r.updateStatefulSet(ctx, &instance, &desired, &deployed)
    assert.Nil(t, err)
    assert.NotNil(t, res)
    assert.Equal(t, 10*time.Second, res.RequeueAfter, "should update and then requeue")

    deployed.Status.ReadyReplicas = 0
    deployed.Status.Replicas = 2
    deployed.Spec.ServiceName = "service"

    res, err = r.updateStatefulSet(ctx, &instance, &desired, &deployed)
    assert.Nil(t, err)
    assert.NotNil(t, res)
    assert.Equal(t, 15*time.Second, res.RequeueAfter, "should requeue to wait for replicas")
}

func TestUpdateJob(t *testing.T) {
    deployed := batchv1.Job{}
    r := OpenNMSReconciler{}

    deployed.Status.Succeeded = 0

    res := r.updateJob(&deployed)
    assert.NotNil(t, res)
    assert.Equal(t, 10*time.Second, res.RequeueAfter, "should requeue to wait for job to finish")

    deployed.Status.Succeeded = 1

    res = r.updateJob(&deployed)
    assert.Nil(t, res, "noop when job is complete")
}

func TestUpdateSecret(t *testing.T) {
    //TODO HS-495
    r := OpenNMSReconciler{}
    res, err := r.updateSecret(nil, nil, nil)
    assert.Nil(t, res)
    assert.Nil(t, err)
}

func TestUpdateConfigMap(t *testing.T) {
    instance := v1alpha1.OpenNMS{}
    instance.SetName("instance")
    client := fake.NewClientBuilder().WithScheme(scheme.GetScheme()).WithObjects(&instance).Build()
    r := OpenNMSReconciler{Client: client}
    ctx := context.Background()

    deployed := corev1.ConfigMap{}
    deployed.SetName("name")
    desired := corev1.ConfigMap{}
    desired.SetName("name")

    res, err := r.updateConfigMap(ctx, &instance, &desired, &deployed)
    assert.Nil(t, res)
    assert.Nil(t, err, "noop if desired and deployed are the same")

    desired.Data = map[string]string{
        "key": "value",
    }

    res, err = r.updateConfigMap(ctx, &instance, &desired, &deployed)
    assert.NotNil(t, err, "should error if resource being updated doesn't exist")

    err = client.Create(ctx, &deployed)
    assert.Nil(t, err)

    res, err = r.updateConfigMap(ctx, &instance, &desired, &deployed)
    assert.Nil(t, err)
    assert.NotNil(t, res)
    assert.Equal(t, 10*time.Second, res.RequeueAfter, "should update and then requeue")
}
