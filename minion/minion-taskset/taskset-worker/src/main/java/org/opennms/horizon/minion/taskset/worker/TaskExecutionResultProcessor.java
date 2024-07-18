/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.horizon.minion.taskset.worker;

import org.opennms.horizon.minion.plugin.api.CollectionSet;
import org.opennms.horizon.minion.plugin.api.ScanResultsResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorRequest;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.taskset.contract.TaskDefinition;

public interface TaskExecutionResultProcessor {
    /**
     * Queue the given scan result to be sent out.
     *
     * @param uuid
     * @param scanResultsResponse
     */
    void queueSendResult(String uuid, ScanResultsResponse scanResultsResponse);

    /**
     * Queue the given monitor result to be sent out.
     *
     * @param taskDefinition
     * @param serviceMonitorRequest
     * @param serviceMonitorResponse
     */
    void queueSendResult(
            TaskDefinition taskDefinition,
            ServiceMonitorRequest serviceMonitorRequest,
            ServiceMonitorResponse serviceMonitorResponse);

    void queueSendResult(TaskDefinition taskDefinition, CollectionSet collectionSet);
}
