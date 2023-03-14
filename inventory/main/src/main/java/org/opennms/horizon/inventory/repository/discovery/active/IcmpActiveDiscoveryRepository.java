/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.horizon.inventory.repository.discovery.active;

import org.opennms.horizon.inventory.model.discovery.active.IcmpActiveDiscovery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IcmpActiveDiscoveryRepository extends JpaRepository<IcmpActiveDiscovery, Long> {
    List<IcmpActiveDiscovery> findByLocationAndTenantId(String location, String tenantId);

    Optional<IcmpActiveDiscovery> findByLocationAndName(String location, String name);

    List<IcmpActiveDiscovery> findByNameAndTenantId(String name, String tenantId);

    List<IcmpActiveDiscovery> findByTenantId(String tenantId);

    Optional<IcmpActiveDiscovery> findByIdAndTenantId(long id, String tenantId);
}