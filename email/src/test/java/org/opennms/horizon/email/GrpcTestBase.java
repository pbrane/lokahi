package org.opennms.horizon.email;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import org.keycloak.common.VerificationException;
import org.opennms.horizon.email.grpc.EmailServerInterceptor;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public abstract class GrpcTestBase {

    @DynamicPropertySource
    private static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("grpc.server.port", () -> 6767);
    }

    protected final String tenantId = "test-tenant";
    protected final String alternativeTenantId = "other-tenant";
    protected final String authHeader = "Bearer esgs12345";
    protected final String headerWithoutTenant = "Bearer esgs12345invalid";
    protected final String differentTenantHeader = "Bearer esgs12345different";
    protected ManagedChannel channel;
    @Autowired
    @SpyBean
    protected EmailServerInterceptor spyInterceptor;

    protected void prepareServer() throws VerificationException {
        channel = ManagedChannelBuilder.forAddress("localhost", 6767)
            .usePlaintext().build();
        doReturn(Optional.of(tenantId)).when(spyInterceptor).verifyAccessToken(authHeader);
        doReturn(Optional.of(alternativeTenantId)).when(spyInterceptor).verifyAccessToken(differentTenantHeader);
        doReturn(Optional.empty()).when(spyInterceptor).verifyAccessToken(headerWithoutTenant);
        doThrow(new VerificationException()).when(spyInterceptor).verifyAccessToken(null);
    }

    protected void afterTest() throws InterruptedException {
        channel.shutdownNow();
        channel.awaitTermination(10, TimeUnit.SECONDS);
        verifyNoMoreInteractions(spyInterceptor);
        reset(spyInterceptor);
    }

    protected Metadata createAuthHeader(String value) {
        Metadata headers = new Metadata();
        headers.put(GrpcConstants.AUTHORIZATION_METADATA_KEY, value);
        return headers;
    }
}
