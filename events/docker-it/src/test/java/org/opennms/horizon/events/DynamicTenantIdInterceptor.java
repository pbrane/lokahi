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
package org.opennms.horizon.events;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import io.grpc.*;
import org.keycloak.util.TokenUtil;
import org.opennms.horizon.shared.constants.GrpcConstants;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

public class DynamicTenantIdInterceptor implements ClientInterceptor {

    private final Algorithm algorithm;
    private String tenantId;

    public DynamicTenantIdInterceptor(KeyPair keyPair) {
        Objects.requireNonNull(keyPair);
        algorithm = Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new HeaderAttachingClientCall<>(next.newCall(method, callOptions));
    }

    private final class HeaderAttachingClientCall<ReqT, RespT>
            extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {

        HeaderAttachingClientCall(ClientCall<ReqT, RespT> call) {
            super(call);
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            headers.merge(prepareGrpcHeaders());
            super.start(responseListener, headers);
        }
    }

    private Metadata prepareGrpcHeaders() {
        try {
            String token = JWT.create()
                    .withIssuer("test")
                    .withSubject("test")
                    .withClaim("typ", TokenUtil.TOKEN_TYPE_BEARER)
                    .withClaim(GrpcConstants.TENANT_ID_KEY, tenantId)
                    .sign(algorithm);

            Metadata result = new Metadata();
            result.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, "Bearer " + token);
            return result;
        } catch (JWTCreationException e) {
            throw new RuntimeException(e);
        }
    }
}
