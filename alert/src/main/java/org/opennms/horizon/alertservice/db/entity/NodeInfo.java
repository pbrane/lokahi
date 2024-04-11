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
package org.opennms.horizon.alertservice.db.entity;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NodeInfo {
    // Azure related stuff and Monitored State are not included
    private long id;
    private String tenantId;
    private String nodeLabel;
    private long createTime;
    private long monitoringLocationId;
    private List<IpInterface> ipInterfacesList;
    private String objectId;
    private String systemName;
    private String systemDescr;
    private String systemLocation;
    private String systemContact;
    private List<SnmpInterface> snmpInterfacesList;
    private List<NodeTag> tagsList;
    private String location;
    private String nodeAlias;
    private List<Long> discoveryIdsList;
}
