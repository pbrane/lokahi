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
package org.opennms.horizon.inventory.snmp.part;

import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

@Builder
@Value
public class ScalarPart implements Part {

    @NonNull
    @Singular
    public List<Element> elements;

    @Value
    @Builder
    public static class Element {
        @NonNull
        public String oid;

        @NonNull
        public String alias;

        @NonNull
        public String type;
    }

    @Override
    public Optional<ScalarPart> asScalar() {
        return Optional.of(this);
    }

    @Override
    public Optional<TablePart> asTable() {
        return Optional.empty();
    }
}