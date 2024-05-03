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
package org.opennms.horizon.minion.syslog.listener.parser;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogParser {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogParser.class);
    private static final Pattern m_messageIdPattern = Pattern.compile("^((\\S+):\\s*)");
    // date pattern has been updated to support space at start and end of
    // the message due to which the date match failed and current system date
    // used to be passed which cause 3ms more time in parsing message
    private static final Pattern m_datePattern = Pattern.compile("^\\s*((\\d\\d\\d\\d-\\d\\d-\\d\\d)\\s*)");
    private static final Pattern m_oldDatePattern =
            Pattern.compile("^\\s*(\\S\\S\\S\\s+\\d{1,2}\\s+\\d\\d:\\d\\d:\\d\\d)\\s+");

    public SyslogMessage parse(String message) {
        SyslogMessage entry = new SyslogMessage();

        // Extracting priority
        int endIndex = message.indexOf('>') + 1;
        entry.setPriority(Integer.parseInt(message.substring(1, endIndex - 1)));

        // Extracting header and message parts
        String[] parts = message.substring(endIndex).split(" ", 6);

        // Assigning parts to fields (simplified parsing, assuming well-formed messages)
        entry.setVersion(parts[0]);
        entry.setTimestamp(parts[1]);
        entry.setHostname(parts[2]);
        entry.setApplication(parts[3]);
        entry.setMessageId(parts[4]);
        entry.setMessage(parts[5]);
        return entry;
    }
}
