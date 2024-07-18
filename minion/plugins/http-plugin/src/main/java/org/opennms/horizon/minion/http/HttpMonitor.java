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
package org.opennms.horizon.minion.http;

import com.google.common.base.Strings;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.opennms.horizon.minion.plugin.api.PollStatus;
import org.opennms.horizon.minion.plugin.api.ServiceMonitor;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponse;
import org.opennms.horizon.minion.plugin.api.ServiceMonitorResponseImpl;
import org.opennms.horizon.shared.utils.Base64;
import org.opennms.horizon.shared.utils.IPLike;
import org.opennms.horizon.shared.utils.InetAddressUtils;
import org.opennms.monitors.http.contract.AuthParams;
import org.opennms.monitors.http.contract.HttpMonitorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpMonitor implements ServiceMonitor {
    public static final Logger LOG = LoggerFactory.getLogger(HttpMonitor.class);

    /**
     * Default HTTP ports.
     */
    private static final int[] DEFAULT_PORTS = {80, 8080, 8888};

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default URL to 'GET'
     */
    private static final String DEFAULT_URL = "/";

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting for data from the
     * monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on read()

    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(Any config) {

        CompletableFuture<ServiceMonitorResponse> future = new CompletableFuture<>();
        HttpMonitorRequest httpMonitorRequest = null;
        try {
            httpMonitorRequest = config.unpack(HttpMonitorRequest.class);

            if ((isBlank(httpMonitorRequest.getInetAddress()) && isBlank(httpMonitorRequest.getHostName()))
                    || (httpMonitorRequest.getPorts().getPortList() == null
                            || httpMonitorRequest.getPorts().getPortList().isEmpty())
                    || isBlank(httpMonitorRequest.getResponseCode())) {
                throw new ValidationException(
                        "ResponseCode, Port, and at least one of InetAddress and HostName must be specified.");
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        if (!config.is(HttpMonitorRequest.class)) {
            throw new IllegalArgumentException("config must be an HttpRequest; type-url=" + config.getTypeUrl());
        }

        // Cycle through the port list
        //
        var currentPort = -1;
        InetAddress addr = null;
        try {
            addr = isNotBlank(httpMonitorRequest.getInetAddress())
                    ? InetAddressUtils.getInetAddress(httpMonitorRequest.getInetAddress())
                    : InetAddress.getByName(httpMonitorRequest.getHostName());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        final HttpMonitorClient httpClient = new HttpMonitorClient(addr, httpMonitorRequest);

        for (var portIndex = 0;
                portIndex < determinePorts(httpMonitorRequest).length
                        && httpClient.getPollStatus() != PollStatus.SERVICE_AVAILABLE;
                portIndex++) {

            currentPort = determinePorts(httpMonitorRequest)[portIndex];
            httpClient.setTimeoutTracker(new TimeoutTracker(httpMonitorRequest, DEFAULT_RETRY, DEFAULT_TIMEOUT));
            httpClient.setCurrentPort(currentPort);
            LOG.info("Port = {}, Address = {}, {}", currentPort, addr, httpClient.getTimeoutTracker());

            String serviceInfo = new StringBuilder(addr.toString())
                    .append(":")
                    .append(httpMonitorRequest.getHostName())
                    .append(":")
                    .append(currentPort)
                    .toString();

            for (httpClient.getTimeoutTracker().reset();
                    httpClient.getTimeoutTracker().shouldRetry()
                            && httpClient.getPollStatus() != PollStatus.SERVICE_AVAILABLE;
                    httpClient.getTimeoutTracker().nextAttempt()) {

                try {
                    httpClient.getTimeoutTracker().startAttempt();
                    httpClient.connect();
                    LOG.info(
                            "HttpMonitor:retry {}",
                            httpClient.getTimeoutTracker().toString());
                    LOG.info("HttpMonitor: connected to host: {} on port: {}", addr, currentPort);

                    httpClient.sendHttpCommand();

                    if (httpClient.isEndOfStream()) {
                        continue;
                    }

                    httpClient.setResponseTime(httpClient.getTimeoutTracker().elapsedTimeInMillis());
                    logResponseTimes(httpClient.getResponseTime(), httpClient.getCurrentLine());

                    if (httpClient.getPollStatus() == PollStatus.SERVICE_AVAILABLE
                            && StringUtils.isNotBlank(httpClient.getResponseText())) {
                        httpClient.setPollStatus(PollStatus.SERVICE_UNAVAILABLE);
                        httpClient.readLinedMatching();

                        if (httpClient.isEndOfStream()) {
                            continue;
                        }

                        httpClient.read();

                        if (!httpClient.isResponseTextFound()) {
                            String message = "Matching text: [" + httpClient.getResponseText()
                                    + "] not found in body of HTTP response for " + serviceInfo;
                            LOG.debug(message);
                            httpClient.setReason("Matching text: [" + httpClient.getResponseText()
                                    + "] not found in body of HTTP response");
                        }
                    }

                } catch (NoRouteToHostException e) {
                    LOG.warn("checkStatus: No route to host exception while polling {}", serviceInfo, e);
                    portIndex = determinePorts(httpMonitorRequest).length; // Will cause outer for(;;) to terminate
                    return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                            .reason("No route to host exception")
                            .status(ServiceMonitorResponse.Status.Down)
                            .build());
                } catch (SocketTimeoutException e) {
                    LOG.info(
                            "checkStatus: HTTP socket connection for service {} timed out with {}",
                            serviceInfo,
                            httpClient.getTimeoutTracker().toString());
                    return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                            .reason("HTTP connection timeout")
                            .status(ServiceMonitorResponse.Status.Down)
                            .build());
                } catch (InterruptedIOException e) {
                    LOG.info(
                            String.format(
                                    "checkStatus: HTTP connection for service {} interrupted after {} bytes transferred with {}",
                                    serviceInfo,
                                    e.bytesTransferred,
                                    httpClient.getTimeoutTracker().toString()),
                            e);
                    return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                            .reason(String.format(
                                    "HTTP connection interrupted, %d bytes transferred", e.bytesTransferred))
                            .status(ServiceMonitorResponse.Status.Down)
                            .build());
                } catch (ConnectException e) {
                    LOG.warn("Connection exception for {}", serviceInfo, e);
                    return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                            .reason("HTTP connection exception on port: "
                                    + determinePorts(httpMonitorRequest)[portIndex] + ": " + e.getMessage())
                            .status(ServiceMonitorResponse.Status.Down)
                            .build());
                } catch (IOException e) {
                    String exceptionClass = e.getClass().getSimpleName();
                    LOG.warn("{} while polling {}", exceptionClass, serviceInfo, e);
                    return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                            .reason("IOException while polling address: " + addr + ": " + e.getMessage())
                            .status(ServiceMonitorResponse.Status.Down)
                            .build());
                } catch (Throwable e) {
                    String exceptionClass = e.getClass().getSimpleName();
                    LOG.warn("Unexpected {} while polling {}", exceptionClass, serviceInfo, e);
                    return CompletableFuture.completedFuture(ServiceMonitorResponseImpl.builder()
                            .reason("Unexpected exception while polling address: " + addr + ": " + e.getMessage())
                            .status(ServiceMonitorResponse.Status.Down)
                            .build());
                } finally {
                    httpClient.closeConnection();
                }
            } // end for (attempts)
        } // end for (ports)

        PollStatus pollStatus = httpClient.determinePollStatusResponse();
        if (pollStatus != null && httpClient.getPollStatus() == PollStatus.SERVICE_AVAILABLE) {
            future.complete(ServiceMonitorResponseImpl.builder()
                    .status(ServiceMonitorResponse.Status.Up)
                    .responseTime(pollStatus.getResponseTime() != null ? pollStatus.getResponseTime() : 0.0)
                    .reason(pollStatus.getReason())
                    .build());
        } else {
            future.complete(ServiceMonitorResponseImpl.builder()
                    .status(ServiceMonitorResponse.Status.Down)
                    .responseTime(pollStatus.getResponseTime() != null ? pollStatus.getResponseTime() : 0.0)
                    .reason(pollStatus.getReason())
                    .build());
        }

        return future;
    }

    private void logResponseTimes(Double responseTime, String line) {
        LOG.info("poll: response= {}", line);
        LOG.info("poll: responseTime= {}ms", responseTime);
    }

    /**
     * <p>wrapSocket</p>
     *
     * @return a {@link java.net.Socket} object.
     * @throws java.io.IOException if any.
     */
    protected SocketWrapper getSocketWrapper() {
        return new DefaultSocketWrapper();
    }

    private static String determineUserAgent(final HttpMonitorRequest httpMonitorRequest) {
        if (isBlank(httpMonitorRequest.getUserAgent())) {
            return "OpenNMS HttpMonitor";
        }
        return httpMonitorRequest.getUserAgent();
    }

    static String determineBasicAuthentication(final HttpMonitorRequest httpMonitorRequest) {
        if (!httpMonitorRequest.hasAuthParams()) {
            return null;
        }

        return buildCredential(httpMonitorRequest.getAuthParams());
    }

    private static String buildCredential(AuthParams authParams) {
        return new String(Base64.encodeBase64((authParams.getBasicAuthParams().getUserName() + ":"
                        + authParams.getBasicAuthParams().getPassword())
                .getBytes()));
    }

    private static String determineResponseText(final HttpMonitorRequest httpMonitorRequest) {
        return httpMonitorRequest.getResponseText();
    }

    private static String determineResponse(final HttpMonitorRequest httpMonitorRequest) {
        return httpMonitorRequest.getResponseCode() != null
                ? httpMonitorRequest.getResponseCode()
                : determineDefaultResponseRange(determineUrl(httpMonitorRequest));
    }

    private static String determineUrl(final HttpMonitorRequest httpMonitorRequest) {
        return httpMonitorRequest.getUrl() != null ? httpMonitorRequest.getUrl() : DEFAULT_URL;
    }

    /**
     * <p>determinePorts</p>
     *
     * @return an array of int.
     */
    protected int[] determinePorts(final HttpMonitorRequest httpMonitorRequest) {
        if (httpMonitorRequest.getPorts().getPortList() != null
                && !httpMonitorRequest.getPorts().getPortList().isEmpty()) {
            return httpMonitorRequest.getPorts().getPortList().stream()
                    .mapToInt(Integer::intValue)
                    .toArray();
        }
        return DEFAULT_PORTS;
    }

    private static String determineDefaultResponseRange(String url) {
        if (url == null || url.equals(DEFAULT_URL)) {
            return "100-499";
        }
        return "100-399";
    }

    private static boolean isNotBlank(String str) {
        return org.apache.commons.lang.StringUtils.isNotBlank(str);
    }

    private static boolean isBlank(String str) {
        return org.apache.commons.lang.StringUtils.isBlank(str);
    }

    final class HttpMonitorClient {
        private double m_responseTime;
        final InetAddress m_addr;

        final HttpMonitorRequest m_httpMonitorRequest;
        String m_httpCmd;
        Socket m_httpSocket;
        private BufferedReader m_lineRdr;
        private String m_currentLine;
        private int m_serviceStatus;
        private String m_reason;
        private final StringBuilder m_html = new StringBuilder();
        private int m_serverResponseCode;
        private TimeoutTracker m_timeoutTracker;
        private int m_currentPort;
        private String m_responseText;
        private boolean m_responseTextFound = false;
        private boolean m_headerFinished = false;

        HttpMonitorClient(final InetAddress addr, final HttpMonitorRequest httpMonitorRequest) {
            m_addr = addr;
            m_httpMonitorRequest = httpMonitorRequest;
            buildCommand();
            m_serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
            m_responseText = determineResponseText(httpMonitorRequest);
        }

        public void read() throws IOException {
            for (int nullCount = 0; nullCount < 2; ) {
                readLinedMatching();
                if (isEndOfStream()) {
                    nullCount++;
                }
            }
        }

        public boolean isResponseTextFound() {
            return m_responseTextFound;
        }

        public void setResponseTextFound(final boolean found) {
            m_responseTextFound = found;
        }

        private String determineVirtualHost(final InetAddress addr, final HttpMonitorRequest httpMonitorRequest) {
            if (!Strings.isNullOrEmpty(httpMonitorRequest.getHostName())) {
                return httpMonitorRequest.getHostName();
            }

            final String host = InetAddressUtils.str(addr);
            // Wrap IPv6 addresses in square brackets
            if (addr instanceof Inet6Address) {
                return "[" + host + "]";
            } else {
                return host;
            }
        }

        public boolean checkCurrentLineMatchesResponseText() {
            if (!m_headerFinished && StringUtils.isEmpty(m_currentLine)) {
                m_headerFinished = true; // Set to true when all HTTP headers has been processed.
            }
            if (!m_headerFinished) { // Skip perform the regex processing over HTTP headers.
                return false;
            }
            if (m_responseText.charAt(0) == '~' && !m_responseTextFound) {
                m_responseTextFound = m_currentLine.matches(m_responseText.substring(1));
            } else {
                m_responseTextFound = (m_currentLine.indexOf(m_responseText) != -1 ? true : false);
            }
            return m_responseTextFound;
        }

        public String getResponseText() {
            return m_responseText;
        }

        public void setResponseText(final String responseText) {
            m_responseText = responseText;
        }

        public TimeoutTracker getTimeoutTracker() {
            return m_timeoutTracker;
        }

        public int getCurrentPort() {
            return m_currentPort;
        }

        public void setCurrentPort(int currentPort) {
            this.m_currentPort = currentPort;
        }

        public HttpMonitorRequest getHttpMonitorRequest() {
            return m_httpMonitorRequest;
        }

        public void setTimeoutTracker(final TimeoutTracker tracker) {
            m_timeoutTracker = tracker;
        }

        public Double getResponseTime() {
            return m_responseTime;
        }

        public void setResponseTime(final double elapsedTimeInMillis) {
            m_responseTime = elapsedTimeInMillis;
        }

        private void connect() throws IOException, SocketException {
            m_httpSocket = new Socket();
            m_httpSocket.connect(new InetSocketAddress(m_addr, m_currentPort), m_timeoutTracker.getConnectionTimeout());
            m_serviceStatus = PollStatus.SERVICE_UNRESPONSIVE;
            m_httpSocket.setSoTimeout(m_timeoutTracker.getSoTimeout());
            m_httpSocket = getSocketWrapper().wrapSocket(m_httpSocket);
        }

        public void closeConnection() {
            try {
                if (m_httpSocket != null) {
                    m_httpSocket.close();
                    m_httpSocket = null;
                }
            } catch (final IOException e) {
                LOG.warn("Error closing socket connection", e);
            }
        }

        public int getPollStatus() {
            return m_serviceStatus;
        }

        public void setPollStatus(final int serviceStatus) {
            m_serviceStatus = serviceStatus;
        }

        public String getCurrentLine() {
            return m_currentLine;
        }

        public int getServerResponse() {
            return m_serverResponseCode;
        }

        private void determineServerInitialResponse() {
            int serverResponseValue = -1;

            if (m_currentLine != null) {

                if (m_currentLine.startsWith("HTTP/")) {
                    serverResponseValue = parseHttpResponse();
                    if (IPLike.matchNumericListOrRange(
                            String.valueOf(serverResponseValue), determineResponse(m_httpMonitorRequest))) {
                        LOG.info("determineServerResponse: valid server response: " + serverResponseValue + " found.");
                        m_serviceStatus = PollStatus.SERVICE_AVAILABLE;
                    } else {
                        m_serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
                        final StringBuilder sb = new StringBuilder();
                        sb.append("HTTP response value: ");
                        sb.append(serverResponseValue);
                        sb.append(". Expecting: ");
                        sb.append(determineResponse(m_httpMonitorRequest));
                        sb.append(".");
                        m_reason = sb.toString();
                    }
                }
            }
            m_serverResponseCode = serverResponseValue;
        }

        private int parseHttpResponse() {
            final StringTokenizer t = new StringTokenizer(m_currentLine);
            if (t.hasMoreTokens()) {
                t.nextToken();
            }

            int serverResponse = -1;
            if (t.hasMoreTokens()) {
                try {
                    serverResponse = Integer.parseInt(t.nextToken());
                } catch (final NumberFormatException nfE) {

                    LOG.info("Error converting response code from host = {}, response = {}", m_addr, m_currentLine);
                }
            }
            return serverResponse;
        }

        public boolean isEndOfStream() {
            if (m_currentLine == null) {
                return true;
            }
            return false;
        }

        public String readLine() throws IOException {
            m_currentLine = m_lineRdr.readLine();
            LOG.debug("\t<<: {}", m_currentLine);
            m_html.append(m_currentLine);
            return m_currentLine;
        }

        public String readLinedMatching() throws IOException {
            readLine();

            if (m_responseText != null && m_currentLine != null && !m_responseTextFound) {
                if (checkCurrentLineMatchesResponseText()) {
                    LOG.info("response-text: " + m_responseText + ": found.");
                    m_serviceStatus = PollStatus.SERVICE_AVAILABLE;
                }
            }
            return m_currentLine;
        }

        public void sendHttpCommand() throws IOException {
            LOG.info("Sending HTTP command: {}", m_httpCmd);

            m_httpSocket.getOutputStream().write(m_httpCmd.getBytes());
            m_lineRdr = new BufferedReader(new InputStreamReader(m_httpSocket.getInputStream()));
            readLine();
            LOG.info("Server response: {}", m_currentLine);
            determineServerInitialResponse();
            m_headerFinished = false; // Clean header flag for each HTTP request.
        }

        private void buildCommand() {
            final StringBuilder sb = new StringBuilder();
            sb.append("GET ").append(determineUrl(m_httpMonitorRequest)).append(" HTTP/1.1\r\n");
            sb.append("Connection: CLOSE \r\n");
            sb.append("Host: ")
                    .append(determineVirtualHost(m_addr, m_httpMonitorRequest))
                    .append("\r\n");
            sb.append("User-Agent: ")
                    .append(determineUserAgent(m_httpMonitorRequest))
                    .append("\r\n");

            if (determineBasicAuthentication(m_httpMonitorRequest) != null) {
                sb.append("Authorization: Basic ")
                        .append(determineBasicAuthentication(m_httpMonitorRequest))
                        .append("\r\n");
            }

            // append headers.
            if (m_httpMonitorRequest.getHeadersList() != null
                    && !m_httpMonitorRequest.getHeadersList().isEmpty()) {
                m_httpMonitorRequest.getHeadersList().stream()
                        .forEach(h -> sb.append(h.getValue()).append("\r\n"));
            }

            sb.append("\r\n");
            final String cmd = sb.toString();
            LOG.info("checkStatus: cmd:\n", cmd);

            m_httpCmd = cmd;
        }

        public void setReason(final String reason) {
            m_reason = reason;
        }

        public String getReason() {
            return m_reason;
        }

        public Socket getHttpSocket() {
            return m_httpSocket;
        }

        public void setHttpSocket(final Socket httpSocket) {
            m_httpSocket = httpSocket;
        }

        protected PollStatus determinePollStatusResponse() {
            if (getPollStatus() == PollStatus.SERVICE_UNAVAILABLE) {
                //
                // Build port string
                //
                final StringBuilder testedPorts = new StringBuilder();
                for (int i = 0; i < determinePorts(getHttpMonitorRequest()).length; i++) {
                    if (i == 0) {
                        testedPorts.append(determinePorts(getHttpMonitorRequest())[0]);
                    } else {
                        testedPorts.append(',').append(determinePorts(getHttpMonitorRequest())[i]);
                    }
                }

                // Add to parameter map

                setReason(getReason() + "/Ports: " + testedPorts.toString());
                LOG.info("checkStatus: Reason: \"" + getReason() + "\"");
                return PollStatus.unavailable(getReason());

            } else if (getPollStatus() == PollStatus.SERVICE_AVAILABLE) {
                return PollStatus.available(getResponseTime());
            } else {
                return PollStatus.get(getPollStatus(), getReason());
            }
        }
    }
}
