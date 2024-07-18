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
package org.opennms.horizon.events.traps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.InvalidProtocolBufferException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import nl.altindag.log.LogCaptor;
import nl.altindag.log.model.LogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opennms.cloud.grpc.minion.Identity;
import org.opennms.horizon.events.proto.Event;
import org.opennms.horizon.events.proto.EventLog;
import org.opennms.horizon.events.xml.Events;
import org.opennms.horizon.events.xml.Log;
import org.opennms.horizon.grpc.traps.contract.TenantLocationSpecificTrapLogDTO;
import org.opennms.horizon.grpc.traps.contract.TrapDTO;

public class TrapsConsumerTest {

    private EventForwarder mockEventForwarder;
    private Function<String, InetAddress> mockInetAddressLookupFunction;
    private TrapLogProtoToEventLogXmlMapper mockTrapLogProtoToXmlMapper;
    private EventLogXmlToProtoMapper mockEventLogXmlToProtoMapper;

    private TenantLocationSpecificTrapLogDTO baseTestTrapLogDTO;
    private InetAddress testInetAddress;

    private TrapsConsumer target;

    private final String UEI = "uei.opennms.org/generic/traps/SNMP_Cold_Start";

    private final String EVENT_LABEL_NAME = "OpenNMS-defined trap event: SNMP_Cold_Start";

    @BeforeEach
    public void setUp() {
        mockEventForwarder = Mockito.mock(EventForwarder.class);
        mockInetAddressLookupFunction = Mockito.mock(Function.class);
        mockTrapLogProtoToXmlMapper = Mockito.mock(TrapLogProtoToEventLogXmlMapper.class);
        mockEventLogXmlToProtoMapper = Mockito.mock(EventLogXmlToProtoMapper.class);

        baseTestTrapLogDTO = TenantLocationSpecificTrapLogDTO.newBuilder()
                .setTenantId("x-tenant-id-x")
                .setLocationId("x-location-x")
                .setTrapAddress("x-trap-address-x")
                .build();

        testInetAddress = Mockito.mock(InetAddress.class);

        Mockito.when(mockInetAddressLookupFunction.apply("x-trap-address-x")).thenReturn(testInetAddress);

        target = new TrapsConsumer();
        target.setEventForwarder(mockEventForwarder);
        target.setTrapLogProtoToXmlMapper(mockTrapLogProtoToXmlMapper);
        target.setEventLogXmlToProtoMapper(mockEventLogXmlToProtoMapper);
    }

    @Test
    void testConsumeNoNewSuspectEvent() {
        //
        // Setup Test Data and Interactions
        //
        TrapDTO suspectTrapDTO =
                TrapDTO.newBuilder().setAgentAddress("x-agent-address-x").build();

        TenantLocationSpecificTrapLogDTO testTrapLogDTO =
                baseTestTrapLogDTO.toBuilder().addTrapDTO(suspectTrapDTO).build();

        Log testXmlEventLog = new Log();
        Events events = new Events();
        testXmlEventLog.setEvents(events);

        Event testEvent = Event.newBuilder()
                .setNodeId(1)
                .setUei(UEI)
                .setEventLabel(EVENT_LABEL_NAME)
                .build();

        EventLog testProtoEventLog = EventLog.newBuilder().addEvents(testEvent).build();

        Mockito.when(mockTrapLogProtoToXmlMapper.convert(testTrapLogDTO)).thenReturn(testXmlEventLog);
        Mockito.when(mockEventLogXmlToProtoMapper.convert(testXmlEventLog, "x-tenant-id-x"))
                .thenReturn(testProtoEventLog);

        byte[] testKafkaPayload = testTrapLogDTO.toByteArray();

        Optional<Event> resEvent = testProtoEventLog.getEventsList().stream().findFirst();
        assertTrue(resEvent.isPresent(), "No event found in the log");
        assertEquals(testEvent.getEventLabel(), resEvent.get().getEventLabel(), "Event label mismatch");
        assertEquals(testEvent.getUei(), resEvent.get().getUei(), "UEI mismatch");

        //
        // Execute
        //
        target.consume(testKafkaPayload);

        //
        // Verify the Results
        //
        Mockito.verify(mockEventForwarder).sendTrapEvents(Mockito.any(EventLog.class));
        Mockito.verify(mockEventForwarder, Mockito.times(0)).sendInternalEvent(Mockito.any(Event.class));
    }

    @Test
    void testConsumeNewSuspectEvent() {
        //
        // Setup Test Data and Interactions
        //
        TrapDTO suspectTrapDTO =
                TrapDTO.newBuilder().setAgentAddress("x-agent-address-x").build();

        TenantLocationSpecificTrapLogDTO testTrapLogDTO = baseTestTrapLogDTO.toBuilder()
                .setTenantId("x-tenant-id-x")
                .setLocationId("x-location-x")
                .setIdentity(Identity.newBuilder().setSystemId("x-system-id-x").build())
                .addTrapDTO(suspectTrapDTO)
                .build();

        org.opennms.horizon.events.xml.Event testXmlEvent = new org.opennms.horizon.events.xml.Event();
        testXmlEvent.setNodeid(-1L);
        testXmlEvent.setUei("x-uei-x");
        testXmlEvent.setCreationTime(new Date());
        testXmlEvent.setDistPoller("x-dist-poller-x");
        testXmlEvent.setInterface("1.2.3.4");

        Log testXmlEventLog = new Log();
        Events events = new Events();
        testXmlEventLog.setEvents(events);
        events.addEvent(testXmlEvent);

        Event testEvent = Event.newBuilder().build();

        EventLog testProtoEventLog = EventLog.newBuilder().addEvents(testEvent).build();

        Mockito.when(mockTrapLogProtoToXmlMapper.convert(testTrapLogDTO)).thenReturn(testXmlEventLog);
        Mockito.when(mockEventLogXmlToProtoMapper.convert(testXmlEventLog, "x-tenant-id-x"))
                .thenReturn(testProtoEventLog);

        byte[] testKafkaPayload = testTrapLogDTO.toByteArray();

        //
        // Execute
        //
        target.consume(testKafkaPayload);

        //
        // Verify the Results
        //
        Mockito.verify(mockEventForwarder).sendTrapEvents(Mockito.any(EventLog.class));
        Mockito.verify(mockEventForwarder).sendInternalEvent(Mockito.any(Event.class));
    }

    @Test
    void testExceptionOnParseProto() {
        //
        // Setup Test Data and Interactions
        //
        byte[] testKafkaPayload = "BAD-PROTO".getBytes(StandardCharsets.UTF_8);

        try (var logCaptor = LogCaptor.forClass(TrapsConsumer.class)) {
            //
            // Execute
            //
            target.consume(testKafkaPayload);

            //
            // Verify the Results
            //
            Predicate<LogEvent> matcher =
                    (logEvent) -> (Objects.equals("Error while parsing traps", logEvent.getMessage())
                            && (logEvent.getArguments().size() == 0)
                            && (logEvent.getThrowable().orElse(null) instanceof InvalidProtocolBufferException));

            assertTrue(logCaptor.getLogEvents().stream().anyMatch(matcher));
        }
    }
}
