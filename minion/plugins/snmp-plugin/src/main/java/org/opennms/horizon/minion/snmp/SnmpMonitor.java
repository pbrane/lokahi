/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.minion.snmp;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponseImpl.ServiceMonitorResponseImplBuilder;
import org.opennms.horizon.shared.snmp.SnmpAgentConfig;
import org.opennms.horizon.shared.snmp.SnmpHelper;
import org.opennms.horizon.shared.snmp.SnmpObjId;
import org.opennms.horizon.shared.snmp.SnmpStrategy;
import org.opennms.horizon.shared.snmp.StrategyResolver;
import org.opennms.horizon.shared.utils.InetAddressUtils;
import org.opennms.horizon.minion.plugin.api.MonitoredService;
import org.opennms.horizon.minion.plugin.api.ParameterMap;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse.Status;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SNMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class SnmpMonitor extends SnmpMonitorStrategy {
    
    public static final Logger LOG = LoggerFactory.getLogger(SnmpMonitor.class);

    /**
     * Default object to collect if "oid" property not available.
     */
    private static final String DEFAULT_OBJECT_IDENTIFIER = ".1.3.6.1.2.1.1.2.0"; // MIB-II
                                                                                // System
                                                                                // Object
                                                                                // Id

    private static final String DEFAULT_REASON_TEMPLATE = "Observed value '${observedValue}' does not meet criteria '${operator} ${operand}'";
    private final StrategyResolver strategyResolver;

    public SnmpMonitor(StrategyResolver strategyResolver) {
        this.strategyResolver = strategyResolver;
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @exception RuntimeException
     *                Thrown for any unrecoverable errors.
     */

//    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(MonitoredService svc, Map<String, Object> parameters) {


        InetAddress ipaddr = svc.getAddress();

        // Retrieve this interface's SNMP peer object
        //
        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
        final String hostAddress = InetAddressUtils.str(ipaddr);

        // Get configuration parameters
        //
        String oid = ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OBJECT_IDENTIFIER);
        String operator = ParameterMap.getKeyedString(parameters, "operator", null);
        String operand = ParameterMap.getKeyedString(parameters, "operand", null);
        String walkstr = ParameterMap.getKeyedString(parameters, "walk", "false");
        String matchstr = ParameterMap.getKeyedString(parameters, "match-all", "true");
        int countMin = ParameterMap.getKeyedInteger(parameters, "minimum", 0);
        int countMax = ParameterMap.getKeyedInteger(parameters, "maximum", 0);
        String reasonTemplate = ParameterMap.getKeyedString(parameters, "reason-template", DEFAULT_REASON_TEMPLATE);
        String hexstr = ParameterMap.getKeyedString(parameters, "hex", "false");

        hex = "true".equalsIgnoreCase(hexstr);
        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));

        // Squirrel the configuration parameters away in a Properties for later expansion if service is down
        Properties svcParams = new Properties();
        svcParams.setProperty("oid", oid);
        svcParams.setProperty("operator", String.valueOf(operator));
        svcParams.setProperty("operand", String.valueOf(operand));
        svcParams.setProperty("walk", walkstr);
        svcParams.setProperty("matchAll", matchstr);
        svcParams.setProperty("minimum", String.valueOf(countMin));
        svcParams.setProperty("maximum", String.valueOf(countMax));
        svcParams.setProperty("timeout", String.valueOf(agentConfig.getTimeout()));
        svcParams.setProperty("retry", String.valueOf(agentConfig.getRetries()));
        svcParams.setProperty("retries", svcParams.getProperty("retry"));
        svcParams.setProperty("ipaddr", hostAddress);
        svcParams.setProperty("port", String.valueOf(agentConfig.getPort()));
        svcParams.setProperty("hex", hexstr);

        try {
            SnmpObjId snmpObjectId = SnmpObjId.get(oid);
            return new SnmpHelper(strategyResolver).getAsync(agentConfig, new SnmpObjId[] {snmpObjectId})
                .<ServiceMonitorResponse>thenApply(result -> {
                    ServiceMonitorResponseImplBuilder builder = ServiceMonitorResponseImpl.builder()
                        .status(Status.Unknown);

                    Map<String, Number> properties = new HashMap<>();
                    if (result[0] != null) {
                        LOG.debug("poll: SNMP poll succeeded, addr={} oid={} value={}", hostAddress, oid, result);

                        if (result[0].isNumeric()) {
                            properties.put("observedValue", result[0].toLong());
                        }

                        if (meetsCriteria(result[0], operator, operand)) {
                            builder.status(Status.Up);
                        } else {
                            builder.status(Status.Down);
                        }
                    } else {
                        LOG.debug("SNMP poll failed, addr={} oid={}", hostAddress, oid);
                        builder.status(Status.Unknown);
                    }

                    builder.properties(properties);
                    return builder.build();
                }).orTimeout(agentConfig.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (NumberFormatException e) {
            LOG.debug("Number operator used in a non-number evaluation", e);
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder().reason(e.getMessage()).status(Status.Unknown).build());
        } catch (IllegalArgumentException e) {
            LOG.debug("Invalid SNMP Criteria", e);
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder().reason(e.getMessage()).status(Status.Unknown).build());
        } catch (Throwable t) {
            LOG.debug("Unexpected exception during SNMP poll of interface {}", hostAddress, t);
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder().reason(t.getMessage()).status(Status.Unknown).build());
        }
    }

}
