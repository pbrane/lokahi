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
package org.opennms.horizon.minion.syslog.listener;

import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty.NettyConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class SyslogRoute extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogRoute.class);

    private String udpPort;

    @Override
    public void configure() throws Exception {

        from("netty:udp://0.0.0.0:" + Integer.valueOf(udpPort)
                        + "?sync=false&allowDefaultCodec=false&disconnectOnNoReply=false&receiveBufferSize="
                        + Integer.MAX_VALUE + "&connectTimeout=500")
                .routeId("syslogListen")
                .process(new AsyncProcessor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        final ByteBuf buffer = exchange.getIn().getBody(ByteBuf.class);

                        // NettyConstants.NETTY_REMOTE_ADDRESS is a SocketAddress type but because
                        // we are listening on an InetAddress, it will always be of type InetAddressSocket
                        InetSocketAddress source =
                                (InetSocketAddress) exchange.getIn().getHeader(NettyConstants.NETTY_REMOTE_ADDRESS);
                        LOG.info(source.getHostName());

                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        String message = new String(buffer.array(), StandardCharsets.UTF_8);
                    }

                    @Override
                    public boolean process(Exchange exchange, AsyncCallback callback) {
                        final ByteBuf buffer = exchange.getIn().getBody(ByteBuf.class);

                        // NettyConstants.NETTY_REMOTE_ADDRESS is a SocketAddress type but because
                        // we are listening on an InetAddress, it will always be of type InetAddressSocket
                        InetSocketAddress source =
                                (InetSocketAddress) exchange.getIn().getHeader(NettyConstants.NETTY_REMOTE_ADDRESS);

                        LOG.info(source.getHostName());
                        LOG.info("Message received : " + exchange.getIn().getBody());
                        return false;
                    }

                    @Override
                    public CompletableFuture<Exchange> processAsync(Exchange exchange) {
                        final ByteBuf buffer = exchange.getIn().getBody(ByteBuf.class);

                        // NettyConstants.NETTY_REMOTE_ADDRESS is a SocketAddress type but because
                        // we are listening on an InetAddress, it will always be of type InetAddressSocket
                        InetSocketAddress source =
                                (InetSocketAddress) exchange.getIn().getHeader(NettyConstants.NETTY_REMOTE_ADDRESS);
                        LOG.info(source.getHostName());
                        return null;
                    }
                });
    }
}
