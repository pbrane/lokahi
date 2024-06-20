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
package org.opennms.horizon.alertservice.resolver;

import static org.opennms.horizon.alertservice.api.AbstractAlertUtil.*;

import java.sql.SQLException;
import java.util.regex.Matcher;
import org.opennms.horizon.alertservice.api.AbstractAlertUtil;
import org.opennms.horizon.alertservice.api.AlertUtilService;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventParameter;
import org.opennms.horizon.events.proto.SnmpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum StandardExpandableParameterResolvers implements ExpandableParameterResolver {
    // // %uei%:%dpname%:%nodeid%:%interface%:%parm[documentRoot]%
    UEI {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_UEI.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getUei();
        }
    },

    DB_ID {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_EVENT_DB_ID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.getDatabaseId() != 0) {
                return Long.toString(event.getDatabaseId());
            } else {
                return "eventid-unknown";
            }
        }
    },

    DPNAME {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_DPNAME.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getLocationId();
        }
    },

    DESCR {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_DESCR.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getDescription();
        }
    },

    LOGMSG {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_LOGMSG.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getLogMessage();
        }
    },

    NODE_ID {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_NODEID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return Long.toString(event.getNodeId());
        }
    },

    HOST {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_HOST.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getHost();
        }
    },

    INTERFACE {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_INTERFACE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getIpAddress();
        }
    },

    IFINDEX {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_IFINDEX.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.getNodeId() > 0) {
                try {
                    return alertUtilService.getifIndex(event.getNodeId(), event.getIpAddress(), event.getTenantId());
                } catch (SQLException e) {
                    // do nothing
                    LOG.info("ifIndex Unavailable for {}:{}", event.getNodeId(), event.getIpAddress(), e);
                }
            }
            return "N/A";
        }
    },

    INTERFACE_ADDRESS {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_INTERFACE_RESOLVE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getIpAddress();
        }
    },

    PRIMARY_INTERFACE {
        @Override
        public boolean matches(final String parm) {
            return AbstractAlertUtil.TAG_PRIMARY_INTERFACE_ADDRESS.equals(parm);
        }

        @Override
        public String getValue(
                final String parm,
                final String parsedParm,
                final Event event,
                final AlertUtilService alertUtilService) {
            if (event.getNodeId() != 0) {
                try {
                    return alertUtilService.getPrimaryInterface(event.getNodeId(), event.getTenantId());
                } catch (SQLException ex) {
                    // do nothing
                    LOG.info("primary interface ipaddr unavailable for node with id: {}", event.getNodeId(), ex);
                }
            }

            return null;
        }
    },

    SNMP_HOST {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMPHOST.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            // TODO: The data does not currently exist. It should be revised to determine the appropriate source for the
            // SNMP host information
            return "event.getSnmphost()";
        }
    },

    SERVICE {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SERVICE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            // TODO: The data does not currently exist. It should be revised to determine the appropriate source for the
            // Service
            return "event.getService()";
        }
    },

    SNMP {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMP.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.hasInfo()) {
                SnmpInfo info = event.getInfo().getSnmp();
                final StringBuilder snmpStr = new StringBuilder(info.getId());

                snmpStr.append(ATTRIB_DELIM).append(info.getVersion());

                if (info.getSpecific() != 0) {
                    snmpStr.append(ATTRIB_DELIM).append(Integer.toString(info.getSpecific()));
                } else {
                    snmpStr.append(ATTRIB_DELIM + "undefined");
                }

                if (info.getGeneric() != 0) {
                    snmpStr.append(ATTRIB_DELIM).append(Integer.toString(info.getGeneric()));
                } else {
                    snmpStr.append(AbstractAlertUtil.ATTRIB_DELIM + "undefined");
                }

                if (!info.getCommunity().isEmpty()) {
                    snmpStr.append(ATTRIB_DELIM).append(info.getCommunity().trim());
                } else {
                    snmpStr.append(ATTRIB_DELIM + "undefined");
                }

                return snmpStr.toString();
            }
            return null;
        }
    },

    SNMP_ID {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMP_ID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.hasInfo()) {
                SnmpInfo info = event.getInfo().getSnmp();
                return info.getId();
            }
            return null;
        }
    },

    SNMP_TRAP_OID {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMP_TRAP_OID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.hasInfo()) {
                SnmpInfo info = event.getInfo().getSnmp();
                return info.getTrapOid();
            }
            return null;
        }
    },

    SNMP_IDTEXT {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMP_IDTEXT.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            // TODO: The data  currently not exist. It should be revised to determine the appropriate source for the
            // idText
            return "idText";
        }
    },

    SNMP_VERSION {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMP_VERSION.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            SnmpInfo info = event.getInfo().getSnmp();
            if (info != null) {
                return info.getVersion();
            }
            return null;
        }
    },

    SNMP_SPECIFIC {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMP_SPECIFIC.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            SnmpInfo info = event.getInfo().getSnmp();
            if (info.getSpecific() != 0) {
                return Integer.toString(info.getSpecific());
            }
            return null;
        }
    },

    SNMP_GENERIC {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMP_GENERIC.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.hasInfo()) {
                SnmpInfo info = event.getInfo().getSnmp();
                if (info.getSpecific() != 0) {
                    return Integer.toString(info.getGeneric());
                }
                return null;
            }
            return null;
        }
    },

    SNMP_COMMUNITY {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SNMP_COMMUNITY.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.hasInfo()) {
                SnmpInfo info = event.getInfo().getSnmp();
                if (!info.getCommunity().isEmpty()) {
                    return info.getCommunity();
                }
                return null;
            }
            return null;
        }
    },

    SEVERITY {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_SEVERITY.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getSeverity().name();
        }
    },

    PARMS_VALUES {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.PARMS_VALUES.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return AbstractAlertUtil.getAllParmValues(event);
        }
    },

    PARMS_NAMES {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.PARMS_NAMES.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return AbstractAlertUtil.getAllParmNames(event);
        }
    },

    PARMS_ALL {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.PARMS_ALL.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return AbstractAlertUtil.getAllParamValues(event);
        }
    },

    NUM_PARAMS {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.NUM_PARMS_STR.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return String.valueOf(event.getParametersList().size());
        }
    },

    PARM_NUM {
        @Override
        public boolean matches(String parm) {
            return parm.startsWith(AbstractAlertUtil.PARM_NUM_PREFIX);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return AbstractAlertUtil.getNumParmValue(parm, event);
        }
    },

    PARM_NAME_NUMBERED {
        @Override
        public boolean matches(String parm) {
            return parm.startsWith(AbstractAlertUtil.PARM_NAME_NUMBERED_PREFIX);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return AbstractAlertUtil.getNumParmName(parm, event);
        }
    },

    PARM {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.PARM_REGEX.matcher(parm).matches();
        }

        @Override
        public String parse(String parm) {
            // Extract the name of the parameter from the 'parm[ZZZ]' string
            final Matcher m = AbstractAlertUtil.PARM_REGEX.matcher(parm);
            if (!m.matches()) {
                throw new IllegalStateException("parse() should not be called if matches() returned false");
            }
            return m.group(1);
        }

        @Override
        public String getValue(String parsm, String parmName, Event event, AlertUtilService alertUtilService) {
            final EventParameter evParm = getParmTrim(parmName, event);
            if (evParm != null) {
                return evParm.getValue();
            }
            return null;
        }
    },

    HARDWARE {
        @Override
        public boolean matches(String parm) {
            return parm.startsWith(AbstractAlertUtil.HARDWARE_BEGIN);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.getNodeId() != 0) {
                String hwFieldValue = alertUtilService.getHardwareFieldValue(parm, event.getNodeId());
                if (hwFieldValue != null) {
                    return hwFieldValue;
                }
            }
            return "Unknown";
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    ASSET {
        @Override
        public boolean matches(String parm) {
            return parm.startsWith(AbstractAlertUtil.ASSET_BEGIN);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.getNodeId() != 0) {
                String assetFieldValue = alertUtilService.getAssetFieldValue(parm, event.getNodeId());
                if (assetFieldValue != null) {
                    return assetFieldValue;
                }
            }
            return "Unknown";
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    NODE_LABEL {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_NODELABEL.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            String nodeLabel = null;
            try {
                nodeLabel = alertUtilService.getNodeLabel(event.getNodeId(), event.getTenantId());
            } catch (SQLException e) {
                // do nothing
                LOG.info("Node Label unavailable for node with id: {}", event.getNodeId(), e);
            }
            if (nodeLabel != null) {
                return "WebSecurityUtils.sanitizeString(nodeLabel)";
            } else {
                return "Unknown";
            }
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    NODE_LOCATION {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_NODELOCATION.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return event.getLocationId();
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    FOREIGN_SOURCE {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_FOREIGNSOURCE.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return "foreign-source";
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    FOREIGN_ID {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_FOREIGNID.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return "foreign-id";
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    },

    IF_ALIAS {
        @Override
        public boolean matches(String parm) {
            return AbstractAlertUtil.TAG_IFALIAS.equals(parm);
        }

        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            if (event.getNodeId() > 0) {
                event.getIpAddress();
                try {
                    return alertUtilService.getIfAlias(event.getNodeId(), event.getIpAddress(), event.getTenantId());
                } catch (SQLException e) {
                    // do nothing
                    LOG.info("ifAlias Unavailable for {}:{}", event.getNodeId(), event.getIpAddress(), e);
                }
            }
            return event.getIpAddress();
        }

        @Override
        public boolean requiresTransaction() {
            return true;
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(StandardExpandableParameterResolvers.class);

    // By default we don't perform any additional parsing
    @Override
    public String parse(String parm) {
        return null;
    }

    // By default we do not require a transaction
    @Override
    public boolean requiresTransaction() {
        return false;
    }
}
