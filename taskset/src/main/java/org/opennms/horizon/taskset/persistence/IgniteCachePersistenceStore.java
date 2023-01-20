/*
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
 */

package org.opennms.horizon.taskset.persistence;

import com.swrve.ratelimitedlogger.RateLimitedLog;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.cache.Cache;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.opennms.taskset.contract.TaskSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgniteCachePersistenceStore implements TaskSetPersistentStore {

    private final static String LOCATION_TASK_SETS_CACHE_NAME = "locationTaskSets";
    private final Logger logger = LoggerFactory.getLogger(IgniteCachePersistenceStore.class);

    private final Ignite ignite;
    private final Cache<TenantKey, TaskSet> locationTaskSetCache;
    private final Map<TenantKey, Set<Listener>> listeners = new ConcurrentHashMap();

    public IgniteCachePersistenceStore(Ignite ignite) {
        this.ignite = ignite;

        this.locationTaskSetCache = ignite.getOrCreateCache(new CacheConfiguration<TenantKey, TaskSet>()
            .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
            .setName(LOCATION_TASK_SETS_CACHE_NAME)
        );

        IgniteCacheListenerAdapter adapter = new IgniteCacheListenerAdapter(listeners);
        MutableCacheEntryListenerConfiguration listenerFactory = new MutableCacheEntryListenerConfiguration<>(
            () -> adapter,
            () -> (entry) -> true, // skip filter as it must be serialized to began work
            false,
            false
        );
        locationTaskSetCache.registerCacheEntryListener(listenerFactory);
    }

    @Override
    public void store(String tenantId, String locationId, TaskSet taskSet) {
        locationTaskSetCache.put(new TenantKey(tenantId, locationId), taskSet);
    }

    @Override
    public TaskSet retrieve(String tenantId, String locationId) {
        return locationTaskSetCache.get(new TenantKey(tenantId, locationId));
    }

    @Override
    public void addListener(String tenantId, String location, Listener listener) {
        TenantKey tenantKey = new TenantKey(tenantId, location);
        if (!listeners.containsKey(tenantKey)) {
            listeners.put(tenantKey, new HashSet<>());
        }

        logger.info("Registered taskset listener for tenant-id: {} and location: {}", tenantId, location);
        this.listeners.get(tenantKey).add(listener);
    }

    static class TenantKey {
        private final String tenantId;
        private final String location;

        public TenantKey(String tenantId, String location) {
            this.tenantId = tenantId;
            this.location = location;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TenantKey)) {
                return false;
            }
            TenantKey tenantKey = (TenantKey) o;
            return Objects.equals(tenantId, tenantKey.tenantId) && Objects.equals(
                location, tenantKey.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId, location);
        }
    }
}
