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
package org.opennms.horizon.server.service.metrics;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String AZURE_MONITOR_TYPE = "AZURE";
    public static final String AZURE_SCAN_TYPE = "AZURE_SCAN";
    public static final String DEFAULT_MONITOR_TYPE = "ICMP";
    public static final String NODE_ID_KEY = "node_id";
    public static final String MONITOR_KEY = "monitor";
    public static final String INSTANCE_KEY = "instance";
    public static final String FIRST_OBSERVATION_TIME = "first_observation_time";

    public static final String QUERY_PREFIX = "query=";
    public static final String NETWORK_IN_BITS = "network_in_bits";
    public static final String NETWORK_OUT_BITS = "network_out_bits";

    public static final String QUERY_FOR_TOTAL_NETWORK_IN_BITS = "irate(ifHCInOctets%s[4m])*8";
    public static final String QUERY_FOR_TOTAL_NETWORK_OUT_BITS = "irate(ifHCOutOctets%s[4m])*8";

    public static final String QUERY_FOR_AZURE_TOTAL_NETWORK_IN_BITS = "avg_over_time(network_in_total_bytes%s[4m])*8";
    public static final String QUERY_FOR_AZURE_TOTAL_NETWORK_OUT_BITS =
            "avg_over_time(network_out_total_bytes%s[4m])*8";

    public static final String BW_IN_PERCENTAGE = "bw_util_network_in";
    public static final String BW_OUT_PERCENTAGE = "bw_util_network_out";

    public static final String REACHABILITY_PERCENTAGE = "reachability_percentage";
    public static final String AVG_RESPONSE_TIME = "avg_response_time_msec";
    public static final String CPU_UTILIZATION = "cpu_utilization";
    public static final String MEMORY_UTILIZATION = "memory_utilization";

    public static final String QUERY_FOR_BW_IN_UTIL_PERCENTAGE =
            "(irate(ifHCInOctets%1$s[4m])*8) " + "/ (ifHighSpeed%1$s *1000000) * 100 unless ifHighSpeed%1$s == 0";
    public static final String QUERY_FOR_BW_OUT_UTIL_PERCENTAGE =
            "(irate(ifHCOutOctets%1$s[4m])*8) " + "/ (ifHighSpeed%1$s *1000000) * 100 unless ifHighSpeed%1$s == 0";

    public static final String NETWORK_ERRORS_IN = "network_errors_in";
    public static final String NETWORK_ERRORS_OUT = "network_errors_out";

    public static final String QUERY_FOR_NETWORK_ERRORS_IN = "irate(ifInErrors%s[4m])";
    public static final String QUERY_FOR_NETWORK_ERRORS_OUT = "irate(ifOutErrors%s[4m])";

    // Total Network
    public static final String TOTAL_NETWORK_BITS_IN = "total_network_bits_in";
    public static final String TOTAL_NETWORK_BITS_OUT = "total_network_bits_out";

    public static final String QUERY_FOR_TOTAL_NETWORK_BITS_IN =
            """
                (sum(irate(ifHCInOctets[4m])) + sum(avg_over_time(network_in_total_bytes[4m])))*8 or vector(0)
                    unless
                count(irate(ifHCInOctets[4m])) == 0 and count(sum_over_time(network_in_total_bytes[1m])) == 0
        """;

    public static final String QUERY_FOR_TOTAL_NETWORK_BITS_OUT =
            """
                (sum(irate(ifHCInOctets[4m])) + sum(avg_over_time(network_out_total_bytes[4m])))*8 or vector(0)
                    unless
                count(irate(ifHCOutOctets[4m])) == 0 and count(sum_over_time(network_out_total_bytes[1m])) == 0
        """;

    public static final String QUERY_FOR_CPU_UTILIZATION =
            """

                (100 -  (irate(CpuRawIdle%1$s[4m]) / (irate(CpuRawIdle%1$s[4m]) + irate(CpuRawInterrupt%1$s[4m]) + irate(CpuRawUser%1$s[4m])
                + irate(CpuRawWait%1$s[4m]) + irate(CpuRawNice%1$s[4m]) + irate(CpuRawSystem%1$s[4m]) + irate(CpuRawKernel%1$s[4m]) + irate(CpuRawSoftIRQ%1$s[4m])  + irate(CpuRawSteal%1$s[4m]) + irate(CpuRawGuest%1$s[4m]) + irate(CpuRawGuestNice%1$s[4m])))  * 100 )
        """;

    public static final String QUERY_FOR_MEMORY_UTILIZATION =
            """
               ((memTotalReal%1$s - memAvailReal%1$s
                - memBuffer%1$s
                - memCached%1$s) / memTotalReal%1$s) * 100

           """;
}
