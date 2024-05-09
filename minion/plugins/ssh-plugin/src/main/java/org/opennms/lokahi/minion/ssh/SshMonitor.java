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
package org.opennms.lokahi.minion.ssh;

import com.google.protobuf.Any;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.opennms.horizon.minion.plugin.api.*;
import org.opennms.inventory.service.ServiceInventory;
import org.opennms.ssh.contract.SshMonitorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshMonitor implements ServiceMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(SshMonitor.class);

    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(Any config) {
        LOG.info("SSH Monitor Pooling");

        SshMonitorRequest sshMonitorRequest = null;

        CompletableFuture<ServiceMonitorResponse> future;

        try {

            if (!config.is(SshMonitorRequest.class)) {
                throw new IllegalArgumentException(
                        "configuration must be an SshMonitorRequest; type-url=" + config.getTypeUrl());
            }

            sshMonitorRequest = config.unpack(SshMonitorRequest.class);
            final ServiceInventory serviceInventory = sshMonitorRequest.getServiceInventory();

            LOG.debug("Address: {}", sshMonitorRequest.getAddress());
            LOG.debug("Port: {}", sshMonitorRequest.getPort());
            LOG.debug("Banner: {}", sshMonitorRequest.getBanner());
            LOG.debug("Client Banner: {}", sshMonitorRequest.getClientBanner());
            LOG.debug("Retries: {}", sshMonitorRequest.getRetry());
            LOG.debug("Timeout: {}", sshMonitorRequest.getTimeout());

            final String ipAddress = sshMonitorRequest.getAddress();

            future = pollAsync(InetAddress.getByName(sshMonitorRequest.getAddress()), sshMonitorRequest, LOG)
                    .thenApply(pollStatus -> {
                        ServiceMonitorResponseImpl serviceMonitorResponse = ServiceMonitorResponseImpl.builder()
                                .reason(pollStatus.getReason())
                                .ipAddress(ipAddress)
                                .status(
                                        pollStatus.getStatusName().equalsIgnoreCase("up")
                                                ? ServiceMonitorResponse.Status.Up
                                                : ServiceMonitorResponse.Status.Down)
                                .nodeId(serviceInventory.getNodeId())
                                .monitoredServiceId(serviceInventory.getMonitorServiceId())
                                .build();
                        return serviceMonitorResponse;
                    });
            return future;
        } catch (Exception e) {
            LOG.debug(
                    "Unexpected exception during SSH poll {}, {}",
                    sshMonitorRequest.getAddress(),
                    sshMonitorRequest.getPort());
            return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                    .reason(e.getMessage())
                    .nodeId(sshMonitorRequest.getServiceInventory().getNodeId())
                    .monitoredServiceId(sshMonitorRequest.getServiceInventory().getMonitorServiceId())
                    .status(ServiceMonitorResponse.Status.Unknown)
                    .build());
        }
    }

    public CompletableFuture<PollStatus> pollAsync(
            InetAddress address, SshMonitorRequest sshMonitorRequest, Logger LOG) {
        return CompletableFuture.supplyAsync(() -> {
            LOG.info("SSH Pooling Start");

            TimeoutTracker tracker = new TimeoutTracker(sshMonitorRequest.getRetry(), sshMonitorRequest.getTimeout());

            int port = sshMonitorRequest.getPort();
            String banner = sshMonitorRequest.getBanner();
            String clientBanner = sshMonitorRequest.getClientBanner();
            PollStatus ps = PollStatus.unavailable();

            LOG.info("Creating SSH Connection");

            Ssh ssh = new Ssh(address, port, tracker.getConnectionTimeout());
            ssh.setClientBanner(clientBanner);

            LOG.info("Created SSH Connection");

            Pattern regex = null;
            try {
                if (!banner.equals("*")) {
                    regex = Pattern.compile(banner);
                    LOG.debug("banner: /{}/", banner);
                }
            } catch (final PatternSyntaxException e) {
                LOG.info("Invalid regular expression for SSH banner match /{}/: {}", banner, e.getMessage());
                return ps;
            }

            LOG.info("SSH Polling");

            for (tracker.reset(); tracker.shouldRetry() && !ps.isAvailable(); tracker.nextAttempt()) {
                try {
                    ps = ssh.poll(tracker);
                } catch (final Exception e) {
                    LOG.error("An error occurred polling host '{}'", sshMonitorRequest.getAddress(), e);
                    break;
                }

                if (!ps.isAvailable()) {
                    // not able to connect, retry
                    continue;
                }

                // If banner matching string is null or wildcard ("*") then we
                // only need to test connectivity and we've got that!
                if (regex == null) {
                    return ps;
                } else {
                    String response = ssh.getServerBanner();

                    if (response == null) {
                        return PollStatus.unavailable("server closed connection before banner was received.");
                    }

                    if (regex.matcher(response).find()) {
                        LOG.debug("isServer: matching response={}", response);
                        return ps;
                    } else {
                        // Got a response but it didn't match... no need to attempt
                        // retries
                        LOG.debug("isServer: NON-matching response={}", response);
                        return PollStatus.unavailable("server responded, but banner did not match '" + banner + "'");
                    }
                }
            }
            return ps;
        });
    }
}
