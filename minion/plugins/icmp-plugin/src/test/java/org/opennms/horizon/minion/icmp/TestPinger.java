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

package org.opennms.horizon.minion.icmp;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.opennms.horizon.shared.icmp.PingResponseCallback;
import org.opennms.horizon.shared.icmp.Pinger;

import java.net.InetAddress;
import java.util.List;

@Slf4j
@Setter
public class TestPinger implements Pinger {
    private boolean handleError = false;
    private boolean handleResponse = false;
    private boolean handleTimeout = false;

    @Override
    public void ping(InetAddress host, long timeout, int retries, int packetsize, int sequenceId, PingResponseCallback cb) throws Exception {
        ping(host, cb);
    }

    @Override
    public void ping(InetAddress host, long timeout, int retries, int sequenceId, PingResponseCallback cb) throws Exception {
        ping(host, cb);
    }

    private void ping(InetAddress host, PingResponseCallback cb) {
        if (handleError) {
            cb.handleError(host, new TestEchoPacket(false), new Exception("Failed to ping"));
        } else if (handleResponse) {
            cb.handleResponse(host, new TestEchoPacket(true));
        } else if (handleTimeout) {
            cb.handleTimeout(host, new TestEchoPacket(false));
        } else {
            throw new RuntimeException("Must set one of the responses");
        }
    }

    @Override
    public Number ping(InetAddress host, long timeout, int retries, int packetsize) throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public Number ping(InetAddress host, long timeout, int retries) throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public Number ping(InetAddress host) throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval, int size) throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public void setAllowFragmentation(boolean allow) throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public void setTrafficClass(int tc) throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public void initialize4() throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public void initialize6() throws Exception {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public boolean isV4Available() {
        throw new NotImplementedException("Not implemented for testing");
    }

    @Override
    public boolean isV6Available() {
        throw new NotImplementedException("Not implemented for testing");
    }
}
