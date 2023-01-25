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
    v1 "k8s.io/api/apps/v1"
    "k8s.io/apimachinery/pkg/types"
    "sigs.k8s.io/controller-runtime/pkg/client/fake"
    "sigs.k8s.io/controller-runtime/pkg/log/zap"
    "sigs.k8s.io/controller-runtime/pkg/reconcile"
    "testing"
)

func TestGetInstance(t *testing.T) {
    ctx := context.Background()
    ints := v1alpha1.OpenNMS{}
    ints.SetName("onmsName")
    req := reconcile.Request{
        NamespacedName: types.NamespacedName{
            Name: ints.GetName(),
        },
    }

    client := fake.NewClientBuilder().WithScheme(scheme.GetScheme()).WithObjects(&ints).Build()
    r := OpenNMSReconciler{
        Client:    client,
        Instances: map[string]*Instance{},
        Log:       zap.New(),
    }

    assert.Empty(t, r.Instances, "map of instances should be empty")

    instance, err := r.getInstance(ctx, req)
    assert.Nil(t, err)
    assert.Equal(t, ints.GetName(), instance.Name, "should return the correct instance")
    assert.NotEmpty(t, r.Instances, "map of instances shouldn't be empty anymore")

    mapI, ok := r.Instances[ints.GetName()]
    assert.True(t, ok)
    assert.Equal(t, ints.GetName(), mapI.Name, "should have entered the correct instance into the map")

    instance, err = r.getInstance(ctx, req)
    assert.Nil(t, err)
    assert.Equal(t, ints.GetName(), instance.Name, "should return the correct updated instance")

    mapI.CRD.Spec.DeployOnly = true
    mapI.Deployed = true
    r.Instances[ints.GetName()] = mapI

    instance, err = r.getInstance(ctx, req)
    assert.Nil(t, err)
    assert.Nil(t, instance, "should return nil when the instance is deployOnly and is done deploying")

    err = client.Delete(ctx, &ints)
    assert.Nil(t, err)

    instance, err = r.getInstance(ctx, req)
    assert.Nil(t, err)
    assert.Nil(t, instance, "should return nil when the instance no longer exists")
}

func TestGetResourceFromCluster(t *testing.T) {
    ctx := context.Background()
    dep := v1.Deployment{}
    dep.SetName("dName")

    client := fake.NewClientBuilder().WithScheme(scheme.GetScheme()).WithObjects(&dep).Build()
    r := OpenNMSReconciler{Client: client}

    res, ok := r.getResourceFromCluster(ctx, &dep)

    assert.True(t, ok, "should return true when the resource exists")
    assert.Equal(t, dep.GetName(), res.GetName(), "should get the correct resource")

    dep.SetName("idontexist")

    res, ok = r.getResourceFromCluster(ctx, &dep)
    assert.False(t, ok, "should return false when the resource doens't exist")
}

func TestUpdateInstance(t *testing.T) {

    ctx := context.Background()
    instance := v1alpha1.OpenNMS{}
    instance.SetName("iName")

    readInstance := v1alpha1.OpenNMS{}

    client := fake.NewClientBuilder().WithScheme(scheme.GetScheme()).WithObjects(&instance).Build()

    r := OpenNMSReconciler{Client: client}
    r.updateStatus(ctx, &instance, false, "not ready")

    err := client.Get(ctx, types.NamespacedName{Name: instance.GetName()}, &readInstance)
    assert.Nil(t, err)

    assert.False(t, readInstance.Status.Readiness.Ready, "should set the ready status to false")
    assert.Equal(t, "not ready", readInstance.Status.Readiness.Reason, "should set the reason")
    assert.NotEqual(t, "", readInstance.Status.Readiness.Timestamp, "should set the timestamp")

    r.updateStatus(ctx, &instance, true, "ready now")

    err = client.Get(ctx, types.NamespacedName{Name: instance.GetName()}, &readInstance)
    assert.Nil(t, err)

    assert.True(t, readInstance.Status.Readiness.Ready, "should set the ready status to true")
    assert.Equal(t, "ready now", readInstance.Status.Readiness.Reason, "should set the reason")
}
