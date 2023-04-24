/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.minion.taskset.worker.ignite.resource;

import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.NoSuchComponentException;

/**
 * Realization of {@link BeanRegistry} facade based on OSGi Blueprint API.
 *
 * Standard blueprint API does not provide component (bean) lookup by type, hence it is emulated by facade.
 * Consider this while using this registry as lookup by type might be considered more expensive than lookup by name.
 */
public class BlueprintBeanRegistry implements BeanRegistry {

    private final BlueprintContainer container;

    public BlueprintBeanRegistry(BlueprintContainer container) {
        this.container = container;
    }

    @Override
    public <T> T lookup(Class<T> type) {
        for (String id : container.getComponentIds()) {
            Object bean = container.getComponentInstance(id);
            if (type.isInstance(bean)) {
                return type.cast(bean);
            }
        }
        return null;
    }

    @Override
    public Object lookup(String name) {
        try {
            return container.getComponentInstance(name);
        } catch (NoSuchComponentException e) {
            return null;
        }
    }
}
