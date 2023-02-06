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

package org.opennms.horizon.minion.flows.adapter.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.opennms.horizon.grpc.telemetry.contract.ContextKey;
import org.opennms.horizon.grpc.telemetry.contract.TelemetryMessage;
import org.opennms.horizon.minion.flows.adapter.copied.Flow;
import org.opennms.horizon.minion.flows.adapter.copied.FlowSource;
import org.opennms.horizon.minion.flows.adapter.copied.ProcessingOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

public class TelemetryMessageProtoCreator {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryMessageProtoCreator.class);

    public static TelemetryMessage createMessage(FlowSource source, List<Flow> flows,
                                                 boolean applicationThresholding,
                                                 boolean applicationDataCollection, List<? extends PackageDefinition> packageDefinition) {
        ContextKey contextKeyProto = ContextKey.newBuilder().setContext(source.getContextKey().getContext()).setKey(source.getContextKey().getKey()).build();
        org.opennms.horizon.grpc.telemetry.contract.FlowSource flowSourceProto = org.opennms.horizon.grpc.telemetry.contract.FlowSource.newBuilder()
            .setLocation(source.getLocation())
            .setContextKey(contextKeyProto).setSourceAddress(source.getSourceAddress()).build();

        // TODO: packageDefinion param needed?
        ProcessingOptions processingOptions = ProcessingOptions.builder()
            .setApplicationThresholding(applicationThresholding)
            .setApplicationDataCollection(applicationDataCollection)
            .setPackages(packageDefinition)
            .build();
        org.opennms.horizon.grpc.telemetry.contract.ProcessingOptions processingOptionsProto = org.opennms.horizon.grpc.telemetry.contract.ProcessingOptions.newBuilder()
            .setApplicationDataCollection(applicationDataCollection)
            .setApplicationThresholding(applicationThresholding)
            .build();
        return TelemetryMessage.newBuilder()
            .setBytes(convertToByteString(flows))
            .setFlowSource(flowSourceProto)
            .setProcessingOptions(processingOptionsProto)
            .build();
    }


    public static ByteString convertToByteString(List<Flow> flows) {
        byte[] byteArray;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(flows);
        } catch (IOException e) {
            LOG.error("Conversion of TelemetryMessage to Bytestring failed: ", e);
        }
        byteArray = baos.toByteArray();
        return ByteString.copyFrom(byteArray);
    }
}
