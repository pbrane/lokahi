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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;

public abstract class InputSanitizer {

    private static final Safelist SAFE_LIST = Safelist.basic()
            .addTags("b", "i", "u", "p", "h1", "h2", "h3", "h4", "h5", "h6")
            .addTags("img")
            .addAttributes("img", "src")
            .addTags("a")
            .addAttributes("a", "href")
            .addAttributes(":all", "style");

    /**
     * <p>sanitizeString</p>
     *
     * @param raw a {@link String} object.
     * @return a {@link String} object.
     */
    public static String sanitizeString(String raw) {
        return sanitizeString(raw, false);
    }

    /**
     * <p>sanitizeString</p>
     *
     * @param raw a {@link String} object.
     * @param allowHTML a boolean.
     * @return a {@link String} object.
     */
    public static String sanitizeString(String raw, boolean allowHTML) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        String next;

        if (allowHTML) {
            next = Jsoup.clean(raw, SAFE_LIST);
        } else {
            next = Entities.escape(raw);
        }
        return next;
    }
}
