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
package org.opennms.horizon.alertservice.service;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.alertservice.api.AbstractAlertUtil;
import org.opennms.horizon.alertservice.db.entity.IpInterface;
import org.opennms.horizon.alertservice.db.entity.Node;
import org.opennms.horizon.alertservice.db.entity.NodeInfo;
import org.opennms.horizon.alertservice.db.entity.SnmpInterface;
import org.opennms.horizon.alertservice.db.repository.NodeRepository;
import org.opennms.horizon.alertservice.db.tenant.TenantLookup;
import org.opennms.horizon.events.proto.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertUtilServiceImpl extends AbstractAlertUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AlertUtilServiceImpl.class);
    private final TenantLookup tenantLookup;

    @Autowired
    NodeRepository nodeRepository;

    @Override
    public String getHardwareFieldValue(String parm, long nodeId) {
        return " getHardwareFieldValue ";
    }

    @Override
    public String expandParms(String inp, Event event) {
        return super.expandParms(inp, event, null);
    }

    @Override
    public String expandParms(String input, Event event, Map<String, Map<String, String>> decode) {
        return super.expandParms(input, event, decode);
    }

    @Override
    public String getHostName(long nodeId, String hostip, String tenantId) throws SQLException {
        Node node = getNodeByIdAndTenantId(nodeId, tenantId);
        node.getNodeInfo().getIpInterfacesList().stream()
                .filter(x -> x.getHostname().equals(hostip))
                .map(x -> String.valueOf(x.getId()))
                .findFirst()
                .orElse("host-name");
        return "";
    }

    @Override
    public String getIfAlias(long nodeId, String ipAddr, String tenantId) throws SQLException {
        Node node = getNodeByIdAndTenantId(nodeId, tenantId);

        return node.getNodeInfo().getIpInterfacesList().stream()
                .filter(x -> x.getIpAddress().equals(ipAddr))
                .findFirst()
                .flatMap(ipInterface -> findSnmpInterfaceIfAlias(node, ipInterface))
                .orElse("if-alias");
    }

    @Override
    public String getAssetFieldValue(String parm, long nodeId) {
        return " Asset Filed";
    }

    @Override
    public String getForeignId(long nodeId) throws SQLException {
        return " Foriegn key ";
    }

    @Override
    public String getForeignSource(long nodeId) throws SQLException {
        return "";
    }

    @Override
    public String getNodeLabel(long nodeId, String tenantId) throws SQLException {
        return Optional.of(getNodeByIdAndTenantId(nodeId, tenantId))
                .map(Node::getNodeLabel)
                .orElse(" node-label");
    }

    @Override
    public String getNodeLocation(long nodeId, String tenantId) throws SQLException {
        return Optional.of(getNodeByIdAndTenantId(nodeId, tenantId))
                .map(Node::getNodeInfo)
                .map(NodeInfo::getLocation)
                .orElse("node-location");
    }

    @Override
    public String getPrimaryInterface(long nodeId, String tenantId) throws SQLException {
        Node node = getNodeByIdAndTenantId(nodeId, tenantId);

        return node.getNodeInfo().getIpInterfacesList().stream()
                .filter(x -> x.isSnmpPrimary())
                .map(x -> String.valueOf(x.getId()))
                .findFirst()
                .orElse("primary-interface");
    }

    @Override
    public String getifIndex(long nodeId, String ipAddr, String tanentId) throws SQLException {
        Node node = getNodeByIdAndTenantId(nodeId, tanentId);

        return node.getNodeInfo().getIpInterfacesList().stream()
                .filter(x -> x.getIpAddress().equals(ipAddr))
                .findFirst()
                .flatMap(ipInterface -> findSnmpInterfaceIfIndex(node, ipInterface))
                .orElse("if-alias");
    }

    public Optional<String> findSnmpInterfaceIfIndex(Node node, IpInterface ipInterface) {
        return node.getNodeInfo().getSnmpInterfacesList().stream()
                .filter(x -> x.getIfIndex() == ipInterface.getIfIndex())
                .map(snmpInterface -> String.valueOf(snmpInterface.getIfIndex()))
                .findFirst();
    }

    public Optional<String> findSnmpInterfaceIfAlias(Node node, IpInterface ipInterface) {
        return node.getNodeInfo().getSnmpInterfacesList().stream()
                .filter(x -> x.getId() == ipInterface.getSnmpInterfaceId())
                .map(SnmpInterface::getIfAlias)
                .findFirst();
    }

    public Node getNodeByIdAndTenantId(long nodeId, String tenantId) {
        return nodeRepository
                .findByIdAndTenantId(nodeId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Node not found for given tenantId and nodeId"));
    }
}
