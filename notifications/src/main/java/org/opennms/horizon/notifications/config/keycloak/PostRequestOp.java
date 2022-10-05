package org.opennms.horizon.notifications.config.keycloak;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public interface PostRequestOp {
    boolean process(HttpUriRequest request, HttpResponse response);
}
