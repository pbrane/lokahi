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
package org.opennms.metrics.threshold.api;

import static org.opennms.metrics.threshold.api.Constants.*;

import java.util.Map;
import org.opennms.horizon.metrics.threshold.proto.AlertRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MetricsRulerExpressionGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsRulerExpressionGenerator.class);
    private static final String OPERATION_NOT_SUPPORTED_FOR_WSMAN_SERVICE = "Operation not supported for WSMAN service";
    private static final String CONDITION_THRESHOLD_FORMAT = "%s   %s ";

    public String generateMetricsRulerExpression(AlertRule alertRule, String metricsThresholdName) {
        if (alertRule == null) {
            throw new IllegalArgumentException("AlertRule must not be null.");
        }
        String condition = alertRule.getCondition();
        String thresholdValue = alertRule.getThresholdValue();
        String labelsQuery = getLabelsQueryString(alertRule.getLabelsMap());
        String expression = String.format(alertRule.getAlertExpression(), labelsQuery) + CONDITION_THRESHOLD_FORMAT;
        return String.format(expression, condition, thresholdValue);
    }

    public String getLabelsQueryString(Map<String, String> labels) {
        StringBuilder sb = new StringBuilder("{");

        int index = 0;
        for (Map.Entry<String, String> param : labels.entrySet()) {
            sb.append(String.format("%s=\"%s\"", param.getKey(), param.getValue()));
            if (index != labels.size() - 1) {
                sb.append(",");
            }
            index++;
        }

        sb.append("}");
        return sb.toString();
    }
}
