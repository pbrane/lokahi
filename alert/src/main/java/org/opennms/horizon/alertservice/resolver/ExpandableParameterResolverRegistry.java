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

import org.opennms.horizon.alertservice.api.AlertUtilService;
import org.opennms.horizon.events.proto.Event;

public class ExpandableParameterResolverRegistry {
    private static final ExpandableParameterResolver NULL_RESOLVER = new ExpandableParameterResolver() {
        @Override
        public String getValue(String parm, String parsedParm, Event event, AlertUtilService alertUtilService) {
            return null;
        }

        @Override
        public String parse(String parm) {
            return null;
        }

        @Override
        public boolean matches(String parm) {
            return false;
        }

        @Override
        public boolean requiresTransaction() {
            return false;
        }
    };

    public ExpandableParameterResolver getResolver(String token) {
        for (StandardExpandableParameterResolvers parameters : StandardExpandableParameterResolvers.values()) {
            if (parameters.matches(token)) {
                return parameters;
            }
        }
        return NULL_RESOLVER;
    }
}
