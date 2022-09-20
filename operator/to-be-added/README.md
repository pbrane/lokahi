Add this to the Wiki.

Now add the changes from the following sections.

KeycloakRealmImport, set password from CRD else generate random one:
```
        credentials:
          - type: password
            value: YX3n$7j@Dek$QrO1W6ax
        email: admin@test.opennms.org
```

For secret: onms-keycloak-initial-admin
* Create a secret with a CRD created one else generate random.

# Core

Document the onmshs service for Jeff service name change to match keycloak dns.

One of the above, 'env:KEYCLOAK_BASE_URL' is in the core deployment. Add the other below. Changed that to 'http://onms-keycloak-http:8080/auth' in the deployment.
* The above is the new service for keycloak with http, need to update everywhere.
* This does not update the org.opennms.core.rest.cfg file on the pod image.

ISSUE: When I try to rebuild the container with the updated org.opennms.core.rest.cfg, it fails, it seems to be expecting the http://keycloak-host. Below I am trying to overwrite it in the pod.

Run the following, need to update this file with credentials and proper URL:
```
cd operator/to-be-added/

# We can deploy the onms-keycloak-initial-admin secret with the same name and
# our own password and username, and keycloak will not over write it.
kubectl -n local-instance get secret onms-keycloak-initial-admin -o jsonpath='{.data.password}' | base64 --decode

# Take the password and change the one in the this file:
vi org.opennms.core.rest.cfg

kubectl -n local-instance create configmap core.cfg \
  --from-file=org.opennms.core.rest.cfg
```

Volume mount to core:
```
spec.template.spec.volumes:
        - name: core-rest-cfg
          configMap:
            name: core.cfg
            defaultMode: 420

spec.template.spec.containers:
          volumeMounts:
            - name: core-rest-cfg
              mountPath: /opt/horizon-stream/etc/org.opennms.core.rest.cfg 
              subPath: org.opennms.core.rest.cfg
```

ISSUE: Unable to verify SSL from core to onms-keycloak service. Either, we need to remove SSL and use http or remove verification of ssl on core.
```
Failed to load URLs from https://onms-keycloak:8443/auth/realms/opennms/.well-known/openid-configuration
javax.net.ssl.SSLHandshakeException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

```

If works, then get all ingress backend traffic back to http.

Apply this, this acts as the domain within the cluster. But, the cert is unsecure:
```
kubectl apply -f keycloak-http-service-test.yaml
```

Do this:
```
env:
            - name: EXTRA_JAVA_OPTS
	      value: "-Djavax.net.ssl.trustStore=/opt/horizon-stream/truststore.jks"

```

Generate truststore.jks with the cert used by the ingress for onmshs:
```
$ keytool -import -alias onmshs -file onmshs.crt -keystore truststore.jks -storepass password123
$ kubectl -n local-instance create configmap core.truststore --from-file=truststore.jks
```

Have users manually create the trust store and add this. Just have users use onmshs domain and provide a cert and truststore.jks with infinite expiration date, just for now. WARNING info, add this.

In skaffold, this is not an issue, I am not sure how this works.

Add this to core deployment:
```

spec.template.spec.volumes:
        - name: truststore
          configMap:
            name: core.truststore
            defaultMode: 420

spec.template.spec.containers:
          env:
            - name: EXTRA_JAVA_OPTS
              value: "-Djavax.net.ssl.trustStore=/opt/horizon-stream/truststore.jks -Djavax.net.ssl.trustStorePassword=password123"
	      value: "-Dsun.security.ssl.allowUnsafeRenegotiation=true" # Tried this one
          volumeMounts:
            - name: truststore
              mountPath: /opt/horizon-stream/truststore.jks
              subPath: truststore.jks

# -Djavax.net.debug=ssl,handshake -Djavax.net.ssl.keyStoreType=PKCS12 -Djavax.net.ssl.keyStore=our-client-certs -Djavax.net.ssl.trustStoreType=jks -Djavax.net.ssl.trustStore=their-server-certs
# value: "-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Djavax.net.ssl.trustStore=/opt/horizon-stream/truststore.jks -Djavax.net.ssl.trustStorePassword=password123 -Djavax.net.ssl.trustStoreType=JKS"


# Maven build: mvn clean compile -U -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true 

# See most recent download of logs.
```

Doing the above produced a bunch of errors that seemed to be related, relation was indicated by this:
```
PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested
```

I think that the struststore was allowing onmshs, but then was blocking something else.

Try this to get different DNS accessible from within cluster:
```
    - name: hostname-strict-backchannel
      value: 'false'
```

We are overwriting the default trust store. Ideally, we want to remove the onmshs service and have a separate backend url from the frontend url.
```
# Add these to access the maven repository, once I added the truststore, it produced an error: -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true 
# ISSUE: Error: 
#   Error resolving artifact org.apache.karaf.features:specs:xml:features:4.3.6: [Could not transfer artifact org.apache.karaf.features:specs:xml:features:4.3.6 from/to central (https://repo1.maven.org/maven2/): transfer failed for https://repo1.maven.org/maven2/org/apache/karaf/features/specs/4.3.6/specs-4.3.6-features.xml]
#   java.io.IOException: Error resolving artifact org.apache.karaf.features:specs:xml:features:4.3.6: [Could not transfer artifact org.apache.karaf.features:specs:xml:features:4.3.6 from/to central (https://repo1.maven.org/maven2/): transfer failed for https://repo1.maven.org/maven2/org/apache/karaf/features/specs/4.3.6/specs-4.3.6-features.xml] 
# ...
#   Caused by: javax.net.ssl.SSLHandshakeException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```

onmshs to work, we need to add the following, ignore the first value that is for debug.
```
            - name: JAVA_TOOL_OPTIONS
              value: "-agentlib:jdwp=transport=dt_socket,server=y,address=5005,suspend=n,quiet=n"
            - name: EXTRA_JAVA_OPTS
              value: "-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Djavax.net.ssl.trustStore=/opt/horizon-stream/truststore.jks -Djavax.net.ssl.trustStorePassword=password123 -Djavax.net.ssl.trustStoreType=JKS"
```
