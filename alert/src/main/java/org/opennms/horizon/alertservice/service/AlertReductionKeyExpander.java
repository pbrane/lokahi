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
package org.opennms.horizon.alertservice.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alertservice.api.AlertUtilService;
import org.opennms.horizon.events.proto.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class AlertReductionKeyExpander implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(AlertReductionKeyExpander.class);

    private final AlertUtilService alertUtilService;

    /**
     * The default event UEI - if the event lookup into the 'event.conf' fails,
     * the event is loaded with information from this default UEI
     */
    private static final String DEFAULT_EVENT_UEI = "uei.opennms.org/default/event";

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(alertUtilService != null, "property alertUtilService must be set");
    }

    public String expandParms(String reductionKey, Event event, Map<String, Map<String, String>> decode) {
        return alertUtilService.expandParms(reductionKey, event);
    }

    public String expandParms(String reductionKey, Event event) {
        return expandParms(reductionKey, event, null);
    }
}
