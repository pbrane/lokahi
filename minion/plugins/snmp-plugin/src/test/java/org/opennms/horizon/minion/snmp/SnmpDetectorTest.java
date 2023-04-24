/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.snmp;

import com.google.protobuf.Any;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.horizon.shared.snmp.SnmpAgentConfig;
import org.opennms.horizon.shared.snmp.SnmpHelper;
import org.opennms.horizon.shared.snmp.SnmpObjId;
import org.opennms.horizon.shared.snmp.SnmpValue;
import org.opennms.inventory.types.ServiceType;
import org.opennms.node.scan.contract.ServiceResult;
import org.opennms.snmp.contract.SnmpDetectorRequest;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SnmpDetectorTest {

    private SnmpDetector target;

    private SnmpHelper mockSnmpHelper;

    private SnmpDetectorRequest testRequest;

    private Any testConfig;

    @Before
    public void setUp() {
        mockSnmpHelper = Mockito.mock(SnmpHelper.class);

        testRequest =
            SnmpDetectorRequest.newBuilder()
                .setHost("127.0.0.1")
                .build();

        testConfig = Any.pack(testRequest);

        target = new SnmpDetector(mockSnmpHelper);
    }

    @Test
    public void testDetect() throws Exception {
        SnmpValue[] snmpValues = {new TestSnmpValue()};
        CompletableFuture<SnmpValue[]> future = CompletableFuture.completedFuture(snmpValues);

        Mockito.when(mockSnmpHelper.getAsync(Mockito.any(SnmpAgentConfig.class), Mockito.any(SnmpObjId[].class))).thenReturn(future);

        CompletableFuture<ServiceResult> response = target.detect("127.0.0.1", testConfig);

        ServiceResult serviceDetectorResponse = response.get();

        assertTrue(serviceDetectorResponse.getStatus());
        assertEquals(ServiceType.SNMP, serviceDetectorResponse.getService());
        assertEquals(testRequest.getHost(), serviceDetectorResponse.getIpAddress());
    }

    @Test
    public void testDetectSnmpGetThrowsException() throws Exception {
        RuntimeException exception = new RuntimeException("Failed to call snmp get");

        Mockito.when(mockSnmpHelper.getAsync(Mockito.any(SnmpAgentConfig.class), Mockito.any(SnmpObjId[].class))).thenThrow(exception);

        CompletableFuture<ServiceResult> response = target.detect("127.0.0.1", testConfig);

        ServiceResult serviceDetectorResponse = response.get();

        assertFalse(serviceDetectorResponse.getStatus());
        assertEquals(ServiceType.SNMP, serviceDetectorResponse.getService());
        assertEquals(testRequest.getHost(), serviceDetectorResponse.getIpAddress());
    }

    private static class TestSnmpValue implements SnmpValue {

        @Override
        public boolean isEndOfMib() {
            return false;
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public boolean isDisplayable() {
            return false;
        }

        @Override
        public boolean isNumeric() {
            return false;
        }

        @Override
        public int toInt() {
            return 0;
        }

        @Override
        public String toDisplayString() {
            return null;
        }

        @Override
        public InetAddress toInetAddress() {
            return null;
        }

        @Override
        public long toLong() {
            return 0;
        }

        @Override
        public BigInteger toBigInteger() {
            return null;
        }

        @Override
        public String toHexString() {
            return null;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public byte[] getBytes() {
            return new byte[0];
        }

        @Override
        public SnmpObjId toSnmpObjId() {
            return null;
        }
    }
}
