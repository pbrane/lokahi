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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String SNMP_EXPRESSION_FOR_CPU_UTILIZATION =
            """
           avg by (node_id) (
                          (100 -
                             (CpuRawIdle%1$s / (
                              CpuRawIdle%1$s +
                              CpuRawInterrupt%1$s +
                              CpuRawUser%1$s+
                              CpuRawWait%1$s +
                              CpuRawNice%1$s +
                              CpuRawSystem%1$s+
                              CpuRawKernel%1$s +
                              CpuRawSoftIRQ%1$s +
                              CpuRawSteal%1$s +
                              CpuRawGuest%1$s +
                              CpuRawGuestNice%1$s
                            )
                          ) * 100
                     ))
    """;

    public static final String WSMAN_EXPRESSION_FOR_CPU_UTILIZATION = """
    """;
    public static final String SNMP_EXPRESSION_FOR_NETWORK_IN_BANDWIDTH =
            """

            avg by(node_id, instance)  (
                       (irate(ifHCInOctets%1$s[4m])*8 / (ifHighSpeed * 1000000) * 100 unless ifHighSpeed == 0) )
    """;

    public static final String WSMAN_EXPRESSION_FOR_NETWORK_IN_BANDWIDTH = """

      """;

    public static final String SNMP_EXPRESSION_FOR_NETWORK_OUT_BANDWIDTH =
            """

             avg by(node_id, instance)  (
                       (irate(ifHCOutOctets%1$s[4m])*8 / (ifHighSpeed * 1000000) * 100 unless ifHighSpeed == 0) )
    """;

    public static final String WSMAN_EXPRESSION_FOR_NETWORK_OUT_BANDWIDTH = """

    """;
}
