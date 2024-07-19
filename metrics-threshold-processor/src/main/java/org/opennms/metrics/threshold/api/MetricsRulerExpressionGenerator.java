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
package org.opennms.metrics.threshold.api;

import org.opennms.horizon.metrics.threshold.proto.AlertRule;
import org.springframework.stereotype.Component;

@Component
public class MetricsRulerExpressionGenerator {

    public String generateMetricsRulerExpression(AlertRule alertRule, String metricsThresholdName) {

        switch (metricsThresholdName) {
            case "cpuUtilization":
                return generateCpuUtilizationExpression(alertRule);
            case "netInUtilization":
                return generateNetworkInBandwidthExpression(alertRule);
            case "netOutUtilization":
                return generateNetworkOutBandwidthExpression(alertRule);
                // Add more cases for other input strings if needed
            default:
                return "";
        }
    }

    private String generateCpuUtilizationExpression(AlertRule alertRule) {
        String baseExpression =
                """
                        avg by (node_id) (
                          (100 -
                             (CpuRawIdle{monitor='SNMP'} / (
                              CpuRawIdle{monitor='SNMP'} +
                              CpuRawInterrupt{monitor='SNMP'} +
                              CpuRawUser{monitor='SNMP'} +
                              CpuRawWait{monitor='SNMP'} +
                              CpuRawNice{monitor='SNMP'} +
                              CpuRawSystem{monitor='SNMP'} +
                              CpuRawKernel{monitor='SNMP'} +
                              CpuRawSoftIRQ{monitor='SNMP'} +
                              CpuRawSteal{monitor='SNMP'} +
                              CpuRawGuest{monitor='SNMP'} +
                              CpuRawGuestNice{monitor='SNMP'}
                            )
                          ) * 100
                        ))  *condition *thresholdValue
                """;
        // Replace placeholders with actual values from alertRule
        baseExpression = baseExpression
                .replace("*condition", alertRule.getCondition())
                .replace("*thresholdValue", alertRule.getThresholdValue());
        return baseExpression;
    }

    private String generateNetworkInBandwidthExpression(AlertRule alertRule) {
        String baseExpression =
                """
                 avg by(node_id, instance)  (
                       (irate(ifHCInOctets[*duration])*8 / (ifHighSpeed * 1000000) * 100 unless ifHighSpeed == 0) )  *condition  *thresholdValue""";
        // Replace placeholders with actual values from alertRule
        baseExpression = baseExpression
                .replace("*condition", alertRule.getCondition())
                .replace("*thresholdValue", alertRule.getThresholdValue())
                .replace("*duration", alertRule.getDuration());
        return baseExpression;
    }

    private String generateNetworkOutBandwidthExpression(AlertRule alertRule) {
        String baseExpression =
                """
                  avg by(node_id, instance)  (
                       (irate(ifHCOutOctets[*duration])*8 / (ifHighSpeed * 1000000) * 100 unless ifHighSpeed == 0) )  *condition  *thresholdValue
            """;
        // Replace placeholders with actual values from alertRule
        baseExpression = baseExpression
                .replace("*condition", alertRule.getCondition())
                .replace("*thresholdValue", alertRule.getThresholdValue())
                .replace("*duration", alertRule.getDuration());
        return baseExpression;
    }
}
