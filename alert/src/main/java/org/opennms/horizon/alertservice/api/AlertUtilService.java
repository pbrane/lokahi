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
package org.opennms.horizon.alertservice.api;

import java.sql.SQLException;
import java.util.Map;
import org.opennms.horizon.alertservice.resolver.ExpandableParameterResolver;
import org.opennms.horizon.events.proto.Event;

public interface AlertUtilService {

    String getHardwareFieldValue(String parm, long nodeId);

    String expandParms(String string, Event event);

    String expandParms(String inp, Event event, Map<String, Map<String, String>> decode);

    String getNamedParmValue(String string, Event event);

    void expandMapValues(Map<String, String> parmMap, Event event);

    String getHostName(long nodeId, String hostip, String tenantId) throws SQLException;

    String getEventHost(Event event);

    /**
     * Retrieve ifAlias from the snmpinterface table of the database given a particular
     * nodeId and ipAddr.
     *
     * @param nodeId
     *            Node identifier
     * @param ipAddr
     *            Interface IP address
     *
     * @return ifAlias Retreived ifAlias
     *
     * @throws SQLException
     *             if database error encountered
     */
    String getIfAlias(long nodeId, String ipAddr, String tenantId) throws SQLException;

    /**
     * Helper method.
     *
     * @param parm
     * @param nodeId
     * @return The value of an asset field based on the nodeid of the event
     */
    String getAssetFieldValue(String parm, long nodeId);

    /**
     * Retrieve foreign id from the node table of the database given a particular nodeId.
     *
     * @param nodeId Node identifier
     * @return foreignId Retrieved foreign id
     * @throws SQLException if database error encountered
     */
    String getForeignId(long nodeId) throws SQLException;

    /**
     * Retrieve foreign source from the node table of the database given a particular
     * nodeId.
     *
     * @param nodeId
     *            Node identifier
     *
     * @return foreignSource Retrieved foreign source
     *
     * @throws SQLException
     *             if database error encountered
     */
    String getForeignSource(long nodeId) throws SQLException;

    /**
     * Retrieve nodeLabel from the node table of the database given a particular
     * nodeId.
     *
     * @param nodeId
     *            Node identifier
     *
     * @return nodeLabel Retreived nodeLabel
     *
     * @throws SQLException
     *             if database error encountered
     */
    String getNodeLabel(long nodeId, String tanentId) throws SQLException;

    /**
     * Retrieve nodeLocation from the node table of the database given a particular
     * nodeId.
     *
     * @param nodeId
     *            Node identifier
     *
     * @return nodeLocation Retrieved nodeLocation
     *
     * @throws SQLException
     *             if database error encountered
     */
    String getNodeLocation(long nodeId, String tenantId) throws SQLException;

    // ExpandableParameterResolver getResolver(String token);

    String getPrimaryInterface(long nodeId, String tenantId) throws SQLException;

    ExpandableParameterResolver getResolver(String token);

    String getifIndex(long nodeId, String ipAddr, String tanentId) throws SQLException;
}
