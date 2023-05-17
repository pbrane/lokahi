/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.traps.shell;

import com.google.protobuf.ByteString;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.horizon.grpc.traps.contract.TrapDTO;
import org.opennms.horizon.grpc.traps.contract.TrapIdentity;
import org.opennms.horizon.minion.traps.listener.TrapSinkModule;
import org.opennms.horizon.shared.ipc.rpc.IpcIdentity;
import org.opennms.horizon.shared.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.horizon.shared.utils.InetAddressUtils;
import org.opennms.horizon.snmp.api.SnmpResult;
import org.opennms.horizon.snmp.api.SnmpValue;
import org.opennms.horizon.snmp.api.SnmpValueType;
import org.opennms.sink.traps.contract.ListenerConfig;
import org.opennms.sink.traps.contract.TrapConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Command(scope = "opennms", name = "traps-stress",
    description = "Generate synthetic SNMP traps for stress testing.")
@Service
public class StressTraps implements Action {

    @Reference
    MessageDispatcherFactory messageDispatcherFactory;
    @Reference
    IpcIdentity identity;
    @Option(name = "n", aliases = "num")
    int numTraps = 100;

    @Override
    public Object execute() {
        System.out.printf("Sending %d traps.\n", numTraps);
        try(var dispatcher = messageDispatcherFactory.createAsyncDispatcher(new TrapSinkModule(
            TrapConfig.newBuilder().setListenerConfig(ListenerConfig.newBuilder()
                .setNumThreads(10)
                .setQueueSize(1000)
                .setBatchSize(10)
                .build()).build(), identity, UUID.randomUUID().toString()))) {
            for (int i = 0; i < numTraps; i++) {
                TrapDTO trap = buildTheTrap();
                dispatcher.send(trap);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static TrapDTO buildTheTrap() {
        String sourceAddress = InetAddressUtils.getLocalHostAddressAsString();

        // Fake, but valid data
        final List<SnmpResult> results = new ArrayList<>();
        SnmpResult snmpResult = SnmpResult.newBuilder()
            .setBase(".1.2.3")
            .setValue(SnmpValue.newBuilder()
                .setType(SnmpValueType.COUNTER32)
                .setValue(ByteString.copyFrom(new byte[]{0x00}))
                .build())
            .build();
        results.add(snmpResult);

        return TrapDTO.newBuilder()
            .setTrapAddress(sourceAddress)
            .setAgentAddress(sourceAddress)
            .setCommunity("public")
            .setVersion("v2c")
            .setTimestamp(System.currentTimeMillis())
            .setPduLength(1)
            .setCreationTime(System.currentTimeMillis())
            .setTrapIdentity(TrapIdentity.newBuilder()
                .setEnterpriseId("")
                .setGeneric(6)
                .setSpecific(1)
                .setTrapOID(".1.2.3")
                .build())
            .addAllSnmpResults(results)
            .build();
    }
}
