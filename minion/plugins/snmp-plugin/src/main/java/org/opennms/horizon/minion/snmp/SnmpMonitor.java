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
package org.opennms.horizon.minion.snmp;

import static org.opennms.horizon.minion.snmp.SnmpMonitorUtils.meetsCriteria;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.opennms.horizon.minion.plugin.api.ServiceMonitor;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse.Status;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponseImpl;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponseImpl.ServiceMonitorResponseImplBuilder;
import org.opennms.horizon.shared.snmp.SnmpAgentConfig;
import org.opennms.horizon.shared.snmp.SnmpHelper;
import org.opennms.horizon.shared.snmp.SnmpObjId;
import org.opennms.horizon.shared.snmp.SnmpValue;
import org.opennms.horizon.shared.snmp.StrategyResolver;
import org.opennms.snmp.contract.SnmpMonitorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TBD888: is there lost logic here?  For example, counting
 *
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
public class SnmpMonitor implements ServiceMonitor {

    public static final long NANOSECOND_PER_MILLISECOND = 1_000_000;

    public static final Logger LOG = LoggerFactory.getLogger(SnmpMonitor.class);

    /**
     * Default object to collect if "oid" property not available.
     */
    private static final String DEFAULT_OBJECT_IDENTIFIER = ".1.3.6.1.2.1.1.2.0"; // MIB-II
    // System
    // Object
    // Id

    private final StrategyResolver strategyResolver;
    private final SnmpHelper snmpHelper;

    private final Descriptors.FieldDescriptor hostFieldDescriptor;
    private final Descriptors.FieldDescriptor oidFieldDescriptor;
    private final Descriptors.FieldDescriptor operatorFieldDescriptor;
    private final Descriptors.FieldDescriptor operandFieldDescriptor;

    public SnmpMonitor(StrategyResolver strategyResolver, SnmpHelper snmpHelper) {
        this.strategyResolver = strategyResolver;
        this.snmpHelper = snmpHelper;

        Descriptors.Descriptor snmpMonitorRequestDescriptor =
                SnmpMonitorRequest.getDefaultInstance().getDescriptorForType();

        hostFieldDescriptor = snmpMonitorRequestDescriptor.findFieldByNumber(SnmpMonitorRequest.HOST_FIELD_NUMBER);
        oidFieldDescriptor = snmpMonitorRequestDescriptor.findFieldByNumber(SnmpMonitorRequest.OID_FIELD_NUMBER);
        operandFieldDescriptor =
                snmpMonitorRequestDescriptor.findFieldByNumber(SnmpMonitorRequest.OPERAND_FIELD_NUMBER);
        operatorFieldDescriptor =
                snmpMonitorRequestDescriptor.findFieldByNumber(SnmpMonitorRequest.OPERATOR_FIELD_NUMBER);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     *
     * @throws RuntimeException Thrown for any unrecoverable errors.
     */
    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(Any config) {

        if (!config.is(SnmpMonitorRequest.class)) {
            throw new IllegalArgumentException("config must be an SnmpRequest; type-url=" + config.getTypeUrl());
        }

        final SnmpMonitorRequest snmpMonitorRequest;
        try {
            snmpMonitorRequest = config.unpack(SnmpMonitorRequest.class);
        } catch (InvalidProtocolBufferException ex) {
            LOG.error("Invalid poller config", ex);
            throw new RuntimeException(ex);
        }

        // Retrieve this interface's SNMP peer object
        final SnmpAgentConfig agentConfig =
                SnmpConfigUtils.mapAgentConfig(snmpMonitorRequest.getHost(), snmpMonitorRequest.getAgentConfig());

        final String hostAddress = snmpMonitorRequest.getHost();

        try {
            // Get configuration parameters
            //
            String oid = snmpMonitorRequest.hasField(oidFieldDescriptor)
                    ? snmpMonitorRequest.getOid()
                    : DEFAULT_OBJECT_IDENTIFIER;
            String operator = protobufDefaultNullHelper(snmpMonitorRequest, operatorFieldDescriptor);
            String operand = protobufDefaultNullHelper(snmpMonitorRequest, operandFieldDescriptor);

            SnmpObjId snmpObjectId = SnmpObjId.get(oid);

            long startTimestamp = System.nanoTime();

            return snmpHelper
                    .getAsync(agentConfig, new SnmpObjId[] {snmpObjectId})
                    .thenApply(result ->
                            processSnmpResponse(result, hostAddress, snmpObjectId, operator, operand, startTimestamp))
                    .completeOnTimeout(this.createTimeoutResponse(), agentConfig.getTimeout(), TimeUnit.MILLISECONDS)
                    .exceptionally(this::createExceptionResponse);
        } catch (NumberFormatException e) {
            LOG.debug("Number operator used in a non-number evaluation", e);
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                    .status(Status.Unknown)
                    .reason(e.getMessage())
                    .build());
        } catch (IllegalArgumentException e) {
            LOG.debug("Invalid SNMP Criteria", e);
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                    .status(Status.Unknown)
                    .reason(e.getMessage())
                    .build());
        } catch (Throwable t) {
            LOG.debug("Unexpected exception during SNMP poll of interface {}", hostAddress, t);
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                    .status(Status.Unknown)
                    .reason(t.getMessage())
                    .build());
        }
    }

    // ========================================
    // Internal Methods
    // ----------------------------------------

    private String protobufDefaultNullHelper(Message msg, Descriptors.FieldDescriptor fieldDescriptor) {
        if (!msg.hasField(fieldDescriptor)) {
            return null;
        }

        return (String) msg.getField(fieldDescriptor);
    }

    private ServiceMonitorResponse processSnmpResponse(
            SnmpValue[] result,
            String hostAddress,
            SnmpObjId oid,
            String operator,
            String operand,
            long startTimestamp) {
        long endTimestamp = System.nanoTime();
        long elapsedTimeNs = (endTimestamp - startTimestamp);
        double elapsedTimeMs = (double) elapsedTimeNs / NANOSECOND_PER_MILLISECOND;

        ServiceMonitorResponseImplBuilder builder =
                ServiceMonitorResponseImpl.builder().status(Status.Unknown).responseTime(elapsedTimeMs);

        Map<String, Double> metrics = new HashMap<>();

        if (result[0] != null) {
            LOG.debug("poll: SNMP poll succeeded, addr={} oid={} value={}", hostAddress, oid, result);

            if (result[0].isNumeric()) {
                metrics.put("observedValue", (double) result[0].toLong());
            }

            if (meetsCriteria(result[0], operator, operand)) {
                builder.status(Status.Up);
            } else {
                builder.status(Status.Down);
            }

            // if (DEFAULT_REASON_TEMPLATE.equals(reasonTemplate)) {
            //     if (operator != null) {
            //         reasonTemplate = "Observed value '${observedValue}' does not meet criteria '${operator}
            // ${operand}'";
            //     } else {
            //         reasonTemplate = "Observed value '${observedValue}' was null";
            //     }
            // }
        } else {
            String reason = "SNMP poll failed, addr=" + hostAddress + " oid=" + oid;
            builder.reason(reason);

            LOG.debug(reason);
        }

        builder.additionalMetrics(metrics);
        return builder.build();
    }

    // NOTE: this is called at call-setup time, not after the timeout.
    private ServiceMonitorResponse createTimeoutResponse() {
        return ServiceMonitorResponseImpl.builder()
                .status(Status.Unknown)
                .reason("timeout")
                .responseTime(-1)
                .build();
    }

    private ServiceMonitorResponse createExceptionResponse(Throwable thrown) {
        LOG.debug("SNMP poll failed", thrown);

        ServiceMonitorResponse response = ServiceMonitorResponseImpl.builder()
                .status(Status.Unknown)
                .reason(thrown.getMessage())
                .build();

        return response;
    }
}
