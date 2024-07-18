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
package org.opennms.core.wsman.utils;

import com.google.protobuf.Descriptors;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;
import org.opennms.wsman.contract.WsmanConfiguration;
import org.opennms.wsman.contract.WsmanDetectorRequest;
import org.opennms.wsman.contract.WsmanMonitorRequest;

public class WsmanEndpointUtils {

    private static String CreateURL(
            InetAddress iNet, boolean strictSsl, boolean gssAuth, String host, int port, String path) throws Exception {
        try {
            String protocol = strictSsl ? "https" : "http";
            path = path == null ? "/wsman" : path;
            // Prepend a forward slash if missing
            path = path.startsWith("/") ? path : "/" + path;
            if (gssAuth) {
                // Always use the canonical host name (FQDN) when using GSS authentication
                host = iNet.getCanonicalHostName();
            }
            return String.format("%s://%s:%s%s", protocol, host, port, path);
        } catch (Exception e) {
            throw new RuntimeException("Invalid endpoint URL: " + e.getMessage());
        }
    }

    private static WSManEndpoint CreateWSManEndpoint(
            URL url, String serverVersion, WsmanConfiguration wsmanConfiguration) throws Exception {

        WSManEndpoint.Builder builder = new WSManEndpoint.Builder(url);
        builder.withServerVersion(WSManVersion.valueOf(serverVersion));
        if (wsmanConfiguration.getGssAuth()) {
            builder.withGSSAuth();
        }
        builder.withStrictSSL(wsmanConfiguration.getStrictSsl());
        Descriptors.Descriptor wsmanMonitorRequestDescriptor = WsmanMonitorRequest.getDescriptor();

        Descriptors.FieldDescriptor wsmanConfigurationFieldDescriptor =
                wsmanMonitorRequestDescriptor.findFieldByNumber(WsmanMonitorRequest.AGENTCONFIGURATION_FIELD_NUMBER);

        Descriptors.Descriptor wsmanConfigurationDescriptor = wsmanConfiguration.getDescriptorForType();

        Descriptors.FieldDescriptor usernameFieldDescriptor =
                wsmanConfigurationDescriptor.findFieldByNumber(WsmanConfiguration.USERNAME_FIELD_NUMBER);
        if (wsmanConfiguration.hasField(usernameFieldDescriptor)) {
            builder.withBasicAuth(wsmanConfiguration.getUsername(), wsmanConfiguration.getPassword());
        }

        Descriptors.FieldDescriptor connectionTimeoutFieldDescriptor =
                wsmanConfigurationDescriptor.findFieldByNumber(WsmanConfiguration.CONNECTION_TIMEOUT_FIELD_NUMBER);
        if (wsmanConfiguration.hasField(connectionTimeoutFieldDescriptor)) {
            builder.withConnectionTimeout(wsmanConfiguration.getConnectionTimeout());
        }

        Descriptors.FieldDescriptor maxElementsFieldDescriptor =
                wsmanConfigurationDescriptor.findFieldByNumber(WsmanConfiguration.MAX_ELEMENTS_FIELD_NUMBER);
        if (wsmanConfiguration.hasField(maxElementsFieldDescriptor)) {
            builder.withMaxElements(wsmanConfiguration.getMaxElements());
        }

        Descriptors.FieldDescriptor maxEnvelopeSizeFieldDescriptor =
                wsmanConfigurationDescriptor.findFieldByNumber(WsmanConfiguration.MAX_ENVELOPE_SIZE_FIELD_NUMBER);
        if (wsmanConfiguration.hasField(maxEnvelopeSizeFieldDescriptor)) {
            builder.withMaxEnvelopeSize(wsmanConfiguration.getMaxEnvelopeSize());
        }

        Descriptors.FieldDescriptor receiveTimeoutFieldDescriptor =
                wsmanConfigurationDescriptor.findFieldByNumber(WsmanConfiguration.RECEIVE_TIMEOUT_FIELD_NUMBER);
        if (wsmanConfiguration.hasField(receiveTimeoutFieldDescriptor)) {
            builder.withReceiveTimeout(wsmanConfiguration.getReceiveTimeout());
        }

        return builder.build();
    }

    public static WSManEndpoint fromWsmanMonitorRequest(InetAddress iNet, WsmanMonitorRequest request)
            throws MalformedURLException {

        java.net.URL url;
        WsmanConfiguration wsmanAgent = request.getAgentConfiguration();

        try {
            url = new URL(CreateURL(
                    iNet,
                    wsmanAgent.getStrictSsl(),
                    wsmanAgent.getGssAuth(),
                    wsmanAgent.getHost(),
                    wsmanAgent.getPort(),
                    wsmanAgent.getPath()));
        } catch (Exception e) {
            throw new RuntimeException("Error creating URL: " + e.getMessage());
        }

        try {
            Descriptors.Descriptor wsmanMonitorRequestDescriptor = WsmanMonitorRequest.getDescriptor();
            Descriptors.FieldDescriptor wsmanConfigurationFieldDescriptor =
                    wsmanMonitorRequestDescriptor.findFieldByNumber(
                            WsmanMonitorRequest.AGENTCONFIGURATION_FIELD_NUMBER);

            if (!request.hasField(wsmanConfigurationFieldDescriptor)) {
                throw new Exception("Wsman Configuration is missing.");
            }
            WsmanConfiguration wsmanConfiguration = request.getAgentConfiguration();
            return CreateWSManEndpoint(url, request.getServerVersion().name(), wsmanConfiguration);

        } catch (Exception e) {
            throw new RuntimeException("Error creating Endpoint: " + e.getMessage());
        }
    }

    public static WSManEndpoint fromWsmanDetectorRequest(InetAddress iNet, WsmanDetectorRequest request)
            throws Exception {

        java.net.URL url;
        WsmanConfiguration wsmanAgent = request.getAgentConfiguration();

        try {
            url = new URL(CreateURL(
                    iNet,
                    wsmanAgent.getStrictSsl(),
                    wsmanAgent.getGssAuth(),
                    wsmanAgent.getHost(),
                    wsmanAgent.getPort(),
                    wsmanAgent.getPath()));
        } catch (Exception e) {
            throw new Exception("Error creating URL: " + e.getMessage());
        }

        try {
            Descriptors.Descriptor wsmanDetectorRequestDescriptor = WsmanDetectorRequest.getDescriptor();
            Descriptors.FieldDescriptor wsmanConfigurationFieldDescriptor =
                    wsmanDetectorRequestDescriptor.findFieldByNumber(
                            WsmanDetectorRequest.AGENTCONFIGURATION_FIELD_NUMBER);

            if (!request.hasField(wsmanConfigurationFieldDescriptor)) {
                throw new Exception("Wsman Configuration is missing.");
            }
            WsmanConfiguration wsmanConfiguration = request.getAgentConfiguration();
            return CreateWSManEndpoint(url, request.getServerVersion().name(), wsmanConfiguration);

        } catch (Exception e) {
            throw new Exception("Error creating Endpoint: " + e.getMessage());
        }
    }
}
