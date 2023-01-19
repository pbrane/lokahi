package org.opennms.horizon.taskset.persistence;

import java.util.Objects;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.opennms.taskset.contract.TaskSet;

public class IgniteCachePersistenceStore implements TaskSetPersistentStore {

    private final static String LOcATION_TASK_SETS_CACHE_NAME = "locationTaskSets";

    private final Ignite ignite;
    private final Cache<TenantKey, TaskSet> locationTaskSetCache;

    public IgniteCachePersistenceStore(Ignite ignite) {
        this.ignite = ignite;

        this.locationTaskSetCache = ignite.getOrCreateCache(new CacheConfiguration<TenantKey, TaskSet>()
            .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
            .setName(LOcATION_TASK_SETS_CACHE_NAME)
        );
    }

    @Override
    public void store(String tenantId, String locationId, TaskSet taskSet) {
        locationTaskSetCache.put(new TenantKey(tenantId, locationId), taskSet);
    }

    @Override
    public TaskSet retrieve(String tenantId, String locationId) {
        return locationTaskSetCache.get(new TenantKey(tenantId, locationId));
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
