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
package org.opennms.horizon.minion.plugin.api;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

public class RelaxedX509ExtendedTrustManager extends X509ExtendedTrustManager {
    /**
     * {@inheritDoc}
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    /**
     * <p>
     * getAcceptedIssuers</p>
     *
     * @return an array of {@link java.security.cert.X509Certificate} objects.
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {}

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine ssle)
            throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine ssle)
            throws CertificateException {}
}
