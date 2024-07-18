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
package org.opennms.lokahi.minion.wsman;

import com.google.protobuf.Any;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.opennms.core.wsman.Identity;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.utils.WsmanEndpointUtils;
import org.opennms.horizon.minion.plugin.api.ServiceDetector;
import org.opennms.inventory.types.ServiceType;
import org.opennms.node.scan.contract.ServiceResult;
import org.opennms.wsman.contract.WsmanConfiguration;
import org.opennms.wsman.contract.WsmanDetectorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsmanDetector implements ServiceDetector {

    public static final Logger LOG = LoggerFactory.getLogger(WsmanDetector.class);

    @Override
    public CompletableFuture<ServiceResult> detect(String host, Any config) {

        LOG.debug("WSMAN Detector invoker ..... ");

        try {
            if (!config.is(WsmanDetectorRequest.class)) {
                throw new IllegalArgumentException(
                        "config must be an WsmanDetectorRequest; type-url=" + config.getTypeUrl());
            }

            WsmanDetectorRequest wsmanDetectorRequest = config.unpack(WsmanDetectorRequest.class);

            CompletableFuture<ServiceResult> future = getAsync(wsmanDetectorRequest, LOG)
                    .thenApply(serviceResult -> {
                        ServiceResult serviceResponse = ServiceResult.newBuilder()
                                .setStatus(serviceResult.getStatus())
                                .setIpAddress(serviceResult.getIpAddress())
                                .setService(serviceResult.getService())
                                .build();
                        return serviceResponse;
                    });

            return future;

        } catch (IllegalArgumentException e) {
            LOG.debug("Invalid WSMAN Criteria during detection of interface {}", host, e);
            return CompletableFuture.completedFuture(getErrorResult(host));
        } catch (Exception e) {
            LOG.debug("Unexpected exception during WSMAN detection of interface {}", host, e);
            return CompletableFuture.completedFuture(getErrorResult(host));
        }
    }

    public CompletableFuture<ServiceResult> getAsync(WsmanDetectorRequest wsmanDetectorRequest, Logger LOG) {

        return CompletableFuture.supplyAsync(() -> {
            boolean serviceStatus = false;

            try {
                WsmanConfiguration wsmanConfiguration = wsmanDetectorRequest.getAgentConfiguration();
                WSManEndpoint endpoint;
                InetAddress address = InetAddress.getByName(wsmanConfiguration.getHost());

                endpoint = WsmanEndpointUtils.fromWsmanDetectorRequest(address, wsmanDetectorRequest);
                WSManClientFactory m_factory = new CXFWSManClientFactory();
                WSManClient client = m_factory.getClient(endpoint);

                Identity identity = null;
                final Map<String, String> attributes = new HashMap<>();
                try {
                    identity = client.identify();

                } catch (WSManException e) {
                    LOG.info("Identify failed for endpoint {} with error {}.", endpoint, e);
                }

                if (identity != null) {
                    serviceStatus = true;
                    LOG.info("UP, Node is UP, Congrats, WSMAN identified/ detected successfully. {} ", endpoint);
                } else LOG.info("Node DOWN, not able to detect {} ", endpoint);

            } catch (Exception e) {
                LOG.debug(
                        "WSMAN - Detection Error: {}, {} ",
                        wsmanDetectorRequest.getAgentConfiguration().getHost(),
                        e.toString());
            }

            return ServiceResult.newBuilder()
                    .setService(ServiceType.WSMAN)
                    .setIpAddress(wsmanDetectorRequest.getAgentConfiguration().getHost())
                    .setStatus(serviceStatus)
                    .build();
        });
    }

    private ServiceResult getResponse(String host, Throwable throwable) {
        if (throwable != null) {
            return getErrorResult(host);
        }
        return getDetectedResult(host);
    }

    private ServiceResult getDetectedResult(String host) {
        return ServiceResult.newBuilder()
                .setService(ServiceType.WSMAN)
                .setIpAddress(host)
                .setStatus(true)
                .build();
    }

    private ServiceResult getErrorResult(String host) {
        return ServiceResult.newBuilder()
                .setService(ServiceType.WSMAN)
                .setIpAddress(host)
                .setStatus(false)
                .build();
    }
}
