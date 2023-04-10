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
	"fmt"
	"net/http"
)

func (r *OpenNMSReconciler) instanceReady(instance *Instance) (bool, error) {
	uiServiceName := instance.Values.Values.OpenNMS.UI.ServiceName
	uiPort := instance.Values.Values.OpenNMS.UI.ServiceValues.Port
	instanceNamespace := instance.Values.Release.Namespace
	url := fmt.Sprintf("http://%s.%s.svc.cluster.local:%d", uiServiceName, instanceNamespace, uiPort)
	req, err := http.NewRequest(http.MethodGet, url, http.NoBody)
	if err != nil {
		r.Log.Error(err, "error creating health check request", "instance", instance.Name)
		return false, err
	}
	res, err := r.HttpClient.Do(req)
	if err != nil {
		r.Log.Error(err, "error communicating with instance", "instance", instance.Name)
		return false, err
	}
	if res.StatusCode != http.StatusOK {
		r.Log.Info("instance isn't ready yet", "instance", instance.Name, "responseCode", res.StatusCode)
		return false, nil
	} else {
		return true, nil
	}
}
