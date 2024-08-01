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
package org.opennms.horizon.server.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class JsonMetricsLabelLoader {

    private static Logger logger = LoggerFactory.getLogger(JsonMetricsLabelLoader.class);

    public static MetricsLabelResponse load() {
        try {

            Path path = Paths.get(new ClassPathResource("metrics-labels.json").getURI());
            String json = new String(Files.readAllBytes(path));

            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(json, MetricsLabelResponse.class);
        } catch (IOException e) {
            logger.error(" Exception  occurs '{}' ", e.getMessage());
        }
        return null;
    }
}
