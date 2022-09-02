FROM quay.io/keycloak/keycloak:18.0.2 as builder
 
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_FEATURES=token-exchange
ENV KC_DB=postgres
 
USER 0
RUN microdnf install tar # allows for Skaffold file sync
 
# Install custom providers
RUN curl -sL https://github.com/aerogear/keycloak-metrics-spi/releases/download/2.5.3/keycloak-metrics-spi-2.5.3.jar -o /opt/keycloak/providers/keycloak-metrics-spi-2.5.3.jar
RUN /opt/keycloak/bin/kc.sh build --http-relative-path=/auth
 
USER 1000
 
FROM quay.io/keycloak/keycloak:19.0.1
COPY --from=builder /opt/keycloak/ /opt/keycloak/
WORKDIR /opt/keycloak
 
COPY --chown=1000 themes themes

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "--verbose"]
