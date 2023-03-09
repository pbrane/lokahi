/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.horizon.alarmservice;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.keycloak.util.TokenUtil;
import org.opennms.horizon.shared.constants.GrpcConstants;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

public class DynamicTenantIdInterceptor implements ClientInterceptor  {

    private final Algorithm algorithm;
    private String tenantId;

    public DynamicTenantIdInterceptor(KeyPair keyPair) {
        Objects.requireNonNull(keyPair);
        algorithm = Algorithm.RSA256((RSAPublicKey)keyPair.getPublic(), (RSAPrivateKey)keyPair.getPrivate());
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
        } catch (JWTCreationException e){
            throw new RuntimeException(e);
        }
    }
}
