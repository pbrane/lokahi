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
package org.opennms.horizon.inventory.snmp.config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@XmlRootElement(name = "storageStrategy", namespace = "http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class StorageStrategy {

    /**
     * Java class name of the class that implements the
     * StorageStrategy.
     */
    @XmlAttribute(name = "class", required = true)
    private String clazz;

    /**
     * list of parameters to pass to the strategy
     *  for strategy-specific configuration information
     */
    @XmlElement(name = "parameter")
    private List<Parameter> parameters = new ArrayList<>();

    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return parameters.remove(parameter);
    }
}
