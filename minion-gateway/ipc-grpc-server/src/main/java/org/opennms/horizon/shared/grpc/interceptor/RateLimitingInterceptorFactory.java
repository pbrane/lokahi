package org.opennms.horizon.shared.grpc.interceptor;

import io.grpc.BindableService;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.apache.ignite.Ignite;

public class RateLimitingInterceptorFactory implements InterceptorFactory {

  private final Ignite ignite;

  public RateLimitingInterceptorFactory(Ignite ignite) {
    this.ignite = ignite;
  }

  @Override
  public BindableService create(BindableService service) {
    ServerServiceDefinition definition = ServerInterceptors.intercept(
      ServerInterceptors.useInputStreamMessages(service.bindService()), new RateLimitingInterceptor(ignite));
    return new BindableService() {
      @Override
      public ServerServiceDefinition bindService() {
        return definition;
      }
    };
  }
}
