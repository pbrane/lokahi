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
package org.opennms.horizon.inventory.monitoring.simple.config;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.opennms.icmp.contract.IcmpMonitorRequest;
import org.opennms.monitors.http.contract.HttpMonitorRequest;
import org.opennms.monitors.http.contract.Port;
import org.opennms.monitors.ntp.contract.NTPMonitorRequest;
import org.opennms.ssh.contract.SshMonitorRequest;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface MonitorConfigMapper {
    HttpMonitorRequest map(HttpMonitorConfig config);

    HttpMonitorRequest map(HttpsMonitorConfig config);

    IcmpMonitorRequest map(IcmpMonitorConfig config);

    NTPMonitorRequest map(NtpMonitorConfig config);

    SshMonitorRequest map(SshMonitorConfig config);

    default Port map(List<Integer> ports) {
        return Port.newBuilder().addAllPort(ports).build();
    }
}
