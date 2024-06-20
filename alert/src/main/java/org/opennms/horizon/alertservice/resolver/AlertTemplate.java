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

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.opennms.horizon.alertservice.api.AlertUtilService;
import org.opennms.horizon.events.proto.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertTemplate implements ExpandableToken {

    private static final Logger LOG = LoggerFactory.getLogger(AlertTemplate.class);

    private static final char PERCENT = '%';

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(".*\\s(?s).*");

    private final String input;

    private final List<ExpandableToken> tokens = Lists.newArrayList();

    private final AlertUtilService alertUtil;

    private final boolean requiresTransaction;

    public AlertTemplate(String input, AlertUtilService eventUtil) {
        this.input = Objects.requireNonNull(input);
        this.alertUtil = Objects.requireNonNull(eventUtil);
        parse();
        this.requiresTransaction = tokens.stream().anyMatch(ExpandableToken::requiresTransaction);
    }

    /**
     * Parses the input and creates {@link ExpandableToken} to expand it.
     */
    private void parse() {
        tokens.clear();
        String tempInp = input;
        int inpLen = input.length();

        int index1 = -1;
        int index2 = -1;

        // check input string to see if it has any %xxx% substring
        while ((tempInp != null) && ((index1 = tempInp.indexOf(PERCENT)) != -1)) {

            LOG.debug("checking input {}", tempInp);
            // copy till first %
            tokens.add(new ExpandableConstant(tempInp.substring(0, index1)));
            tempInp = tempInp.substring(index1);

            index2 = tempInp.indexOf(PERCENT, 1);
            // If another % character is the next value
            if (index2 == 1) {
                tokens.add(new ExpandableConstant(PERCENT));
                tempInp = tempInp.substring(index2 + 1);
                LOG.debug("Escaped percent %% found in value");
            } else if (index2 != -1) {
                // Get the value between the %s
                String parm = tempInp.substring(1, index2);
                LOG.debug("parm: {} found in value", parm);

                // If there's any whitespace in between the % signs, then do not try to
                // expand it with a parameter value
                if (WHITESPACE_PATTERN.matcher(parm).matches()) {
                    tokens.add(new ExpandableConstant(PERCENT));
                    tempInp = tempInp.substring(1);
                    LOG.debug("skipping parm: {} because whitespace found in value", parm);
                    continue;
                }

                tokens.add(new ExpandableParameter(parm, alertUtil));

                if (index2 < (inpLen - 1)) {
                    tempInp = tempInp.substring(index2 + 1);
                } else {
                    tempInp = null;
                }
            } else {
                break;
            }
        }
        if ((index1 == -1 || index2 == -1) && (tempInp != null)) {
            tokens.add(new ExpandableConstant(tempInp));
        }
    }

    @Override
    public String expand(Event event, Map<String, Map<String, String>> decode) {
        return tokens.stream().map(t -> t.expand(event, decode)).collect(Collectors.joining());
    }

    // If we find any token which requires a transaction, the template itself requires a transaction as well
    @Override
    public boolean requiresTransaction() {
        return requiresTransaction;
    }
}
