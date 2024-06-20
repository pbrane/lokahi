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
package org.opennms.horizon.alertservice.resolver;

import java.util.Map;
import java.util.Objects;
import org.opennms.horizon.alertservice.api.AlertUtilService;
import org.opennms.horizon.events.proto.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpandableParameter implements ExpandableToken {

    private static final Logger LOG = LoggerFactory.getLogger(ExpandableParameter.class);

    private final String token;
    private final String parsedToken;
    private final ExpandableParameterResolver resolver;
    private final org.opennms.horizon.alertservice.api.AlertUtilService alertUtilService;

    public ExpandableParameter(String token, AlertUtilService AlertUtilService) {
        this.token = Objects.requireNonNull(token);
        this.resolver = Objects.requireNonNull(AlertUtilService.getResolver(token));
        this.parsedToken = resolver.parse(token);
        this.alertUtilService = Objects.requireNonNull(AlertUtilService);
    }

    @Override
    public String expand(Event event, Map<String, Map<String, String>> decode) {
        String value = resolver.getValue(token, parsedToken, event, alertUtilService);
        LOG.debug("Value of token {}={}", token, value);

        if (value != null) {
            if (decode != null && decode.containsKey(token) && decode.get(token).containsKey(value)) {
                final StringBuilder ret = new StringBuilder();
                ret.append(decode.get(token).get(value));
                ret.append("(");
                ret.append(value);
                ret.append(")");
                return ret.toString();
            } else {

                return InputSanitizer.sanitizeString(value);
            }
        }
        return "";
    }

    @Override
    public boolean requiresTransaction() {
        return resolver.requiresTransaction();
    }
}
