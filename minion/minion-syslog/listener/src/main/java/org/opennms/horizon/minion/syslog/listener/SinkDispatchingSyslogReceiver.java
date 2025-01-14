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

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SinkDispatchingSyslogReceiver implements SyslogReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(SinkDispatchingSyslogReceiver.class);

    private final SyslogdConfig m_config;

    public SinkDispatchingSyslogReceiver(SyslogdConfig config) {
        m_config = Objects.requireNonNull(config);
    }

    @Override
    public void run() {

        // Create an asynchronous dispatcher
        final SyslogSinkModule syslogSinkModule = new SyslogSinkModule(m_config);
    }

    @Override
    public void stop() throws InterruptedException {
        try {

        } catch (Exception e) {
            LOG.warn("Exception while closing dispatcher.", e);
        }
    }
}
