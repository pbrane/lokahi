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
package org.opennms.lokahi.minion.ntp;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import org.opennms.horizon.minion.plugin.api.ServiceMonitor;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponseImpl;
import org.opennms.monitors.ntp.contract.NTPMonitorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NtpMonitor implements ServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(NtpMonitor.class);

    private final int NTP_PACKET_SIZE = 512;

    private final double MILLISECONDS_IN_SECOND = 1000.0;

    private final double NTP_EPOCH_OFFSET = 2208988800.0;

    private int DEFAULT_RETRIES = 3;

    private int DEFAULT_TIME_OUT = 4000;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Poll the specified address for NTP service availability.
     * </P>
     *
     * <p>
     * During the poll an NTP request query packet is generated. The query is
     * sent via UDP socket to the interface at the specified port (by default
     * UDP port 123). If a response is received, it is parsed and validated. If
     * the NTP was successful the service status is set to SERVICE_AVAILABLE and
     * the method returns.
     * </P>
     */
    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(Any config) {
        CompletableFuture<ServiceMonitorResponse> responseCompletableFuture = new CompletableFuture<>();
        if (!config.is(NTPMonitorRequest.class)) {
            throw new IllegalArgumentException(
                    "configuration must be an NTPMonitorRequest; type-url=" + config.getTypeUrl());
        }
        InetAddress ipAddr = null;
        try (DatagramSocket socket = new DatagramSocket()) {

            NTPMonitorRequest request = config.unpack(NTPMonitorRequest.class);
            ipAddr = InetAddress.getByName(request.getInetAddress());
            var retries = request.getRetries() <= 0 ? DEFAULT_RETRIES : request.getRetries();
            var timeOUt = request.getTimeout() <= 0 ? DEFAULT_TIME_OUT : request.getTimeout();
            socket.setSoTimeout(timeOUt);

            for (var i = 0; i < retries; i++) {
                try {
                    LOG.info("Attempting to poll NTP server (retry {})", i + 1);
                    double responseTime = pollNtpServer(socket, ipAddr, request);
                    LOG.info("response time is '{}' ", responseTime);
                    if (responseTime != -1) {
                        LOG.info("NTP polling successful. Response time: {}ms", responseTime);
                        responseCompletableFuture.complete(ServiceMonitorResponseImpl.builder()
                                .responseTime(responseTime)
                                .status(ServiceMonitorResponse.Status.Up)
                                .build());
                        break;
                    }
                } catch (SocketTimeoutException ex) {
                    logTimeout(ipAddr, ex);
                    errorResponse(responseCompletableFuture, ServiceMonitorResponse.Status.Unknown);
                }
            }
        } catch (InvalidProtocolBufferException | UnknownHostException e) {
            LOG.debug("Invalid protocol buffer or unknown host.", e);
            errorResponse(responseCompletableFuture, ServiceMonitorResponse.Status.Down);
        } catch (IOException e) {
            LOG.debug("Error occurred during NTP polling..", e);
            errorResponse(responseCompletableFuture, ServiceMonitorResponse.Status.Down);
        }
        return responseCompletableFuture;
    }

    private double pollNtpServer(DatagramSocket socket, InetAddress ipAddr, NTPMonitorRequest request)
            throws IOException {
        var responseTime = -1.0;
        var startTime = System.currentTimeMillis();
        sendNtpRequest(socket, ipAddr, request);

        NtpMessage msg = receiveNtpResponse(socket);

        var localClockOffset = calculateLocalClockOffset(msg);
        responseTime = System.currentTimeMillis() - startTime;

        logNtpInfo(localClockOffset, responseTime, msg);

        return responseTime;
    }

    private void sendNtpRequest(DatagramSocket socket, InetAddress ipAddr, NTPMonitorRequest request)
            throws IOException {
        byte[] data = new NtpMessage().toByteArray();
        DatagramPacket outgoing = createDatagramPacket(data, ipAddr, request.getPort(0));
        socket.send(outgoing);
    }

    private NtpMessage receiveNtpResponse(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[NTP_PACKET_SIZE];
        DatagramPacket incoming = createDatagramPacket(buffer);
        socket.receive(incoming);
        return new NtpMessage(incoming.getData());
    }

    private double calculateLocalClockOffset(NtpMessage msg) {
        double destinationTimestamp = (System.currentTimeMillis() / MILLISECONDS_IN_SECOND) + NTP_EPOCH_OFFSET;
        return ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;
    }

    private void logNtpInfo(double localClockOffset, double responseTime, NtpMessage msg) {
        LOG.info(
                "poll: valid NTP request received. Local clock offset: {}, response time: {}ms",
                localClockOffset,
                responseTime);
        LOG.info("poll: NTP message: {}", msg);
    }

    private void logTimeout(InetAddress ipAddr, InterruptedIOException ex) {
        LOG.debug("Timeout while connecting to address: {}, {}", ipAddr, ex.getLocalizedMessage());
    }

    private DatagramPacket createDatagramPacket(byte[] data, InetAddress ipAddr, int port) {
        return new DatagramPacket(data, data.length, ipAddr, port);
    }

    private DatagramPacket createDatagramPacket(byte[] buffer) {
        return new DatagramPacket(buffer, buffer.length);
    }

    public void errorResponse(
            CompletableFuture<ServiceMonitorResponse> responseCompletableFuture, ServiceMonitorResponse.Status status) {
        responseCompletableFuture.complete(
                ServiceMonitorResponseImpl.builder().status(status).build());
    }
}
