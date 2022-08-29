/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.ipc.echo;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import org.opennms.horizon.shared.ipc.rpc.api.RpcModule;

public class EchoRpcModule implements RpcModule<EchoRequest, EchoResponse> {

    public static final EchoRpcModule INSTANCE = new EchoRpcModule();

    public static final String RPC_MODULE_ID = "Echo";

    private static final Supplier<Timer> TIMER_SUPPLIER = Suppliers.memoize(() -> new Timer("EchoRpcModule"));

    public EchoRpcModule() {
//        TODO: re-implement this
        throw  new UnsupportedOperationException();

//        try {
//            context = JAXBContext.newInstance(EchoRequest.class, EchoResponse.class);
//        } catch (JAXBException e) {
//            throw new RuntimeException("Failed to initialize EchoRpcModule", e);
//        }
    }

    public void beforeRun() { }

    @Override
    public CompletableFuture<EchoResponse> execute(final EchoRequest request) {
        final CompletableFuture<EchoResponse> future = new CompletableFuture<>();
        if (request.getDelay() != null) {
            TIMER_SUPPLIER.get().schedule(new TimerTask() {
                @Override
                public void run() {
                    processRequest(request, future);
                }
            }, request.getDelay());
        } else {
            processRequest(request, future);
        }
        return future;
    }

    public void processRequest(EchoRequest request, CompletableFuture<EchoResponse> future) {
        beforeRun();
        if (request.shouldThrow()) {
            future.completeExceptionally(new MyEchoException(request.getMessage()));
        } else {
            EchoResponse response = new EchoResponse();
            response.setId(request.getId());
            response.setMessage(request.getMessage());
            response.setBody(request.getBody());
            future.complete(response);
        }
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public Any marshalRequest(EchoRequest echoRequest) {
        return marshal(org.opennms.horizon.grpc.echo.contract.EchoRequest.newBuilder()
            .setId(echoRequest.getId())
            .setMessage(echoRequest.getMessage())
            .setBody(echoRequest.getBody())
            .setDelay(echoRequest.getDelay())
            .setThrow(echoRequest.shouldThrow())
            .build());
    }

    @Override
    public EchoRequest unmarshalRequest(Any request) {
        org.opennms.horizon.grpc.echo.contract.EchoRequest echoRequest = unmarshal(org.opennms.horizon.grpc.echo.contract.EchoRequest.class, request);
        EchoRequest wrapper = new EchoRequest();
        wrapper.setId(echoRequest.getId());
        wrapper.setMessage(echoRequest.getMessage());
        wrapper.setBody(echoRequest.getBody());
        wrapper.setDelay(echoRequest.getDelay());
        wrapper.shouldThrow(echoRequest.getThrow());
        return wrapper;
    }

    @Override
    public Any marshalResponse(EchoResponse response) {
        return marshal(org.opennms.horizon.grpc.echo.contract.EchoResponse.newBuilder()
            .setId(response.getId())
            .setError(response.getErrorMessage())
            .setMessage(response.getMessage())
            .setBody(response.getBody())
            .build());
    }

    @Override
    public EchoResponse unmarshalResponse(Any response) {
        org.opennms.horizon.grpc.echo.contract.EchoResponse echoResponse = unmarshal(org.opennms.horizon.grpc.echo.contract.EchoResponse.class, response);
        EchoResponse wrapper;
        if (!echoResponse.getError().isBlank()) {
            wrapper = new EchoResponse(new IllegalArgumentException(echoResponse.getError()));
        } else {
            wrapper = new EchoResponse(echoResponse.getMessage());
        }
        wrapper.setId(echoResponse.getId());
        wrapper.setMessage(echoResponse.getMessage());
        wrapper.setBody(echoResponse.getBody());
        return wrapper;
    }

    @Override
    public EchoResponse createResponseWithException(Throwable ex) {
        return new EchoResponse(ex);
    }

    private <T extends Message> T unmarshal(Class<T> type, Any payload) {
        try {
            return payload.unpack(type);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Could not unmarshall value", e);
        }
    }

    private <T extends Message> Any marshal(T payload) {
        return Any.pack(payload);
    }

}
