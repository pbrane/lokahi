package org.opennms.miniongateway.router;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorResult;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.cache.CacheEntry;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.cache.CacheMetrics;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.ReadRepairStrategy;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.QueryDetailMetrics;
import org.apache.ignite.cache.query.QueryMetrics;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.mxbean.CacheMetricsMXBean;
import org.apache.ignite.transactions.TransactionException;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.horizon.shared.ignite.remoteasync.MinionLookupService;

public class MinionLookupServiceImplTest {

    @Mock
    private Ignite ignite;

    @Mock
    private IgniteCluster igniteCluster;

    @Mock
    private ClusterNode clusterNode;

    @Mock
    IgniteCache igniteLocationCache;

    @Mock
    IgniteCache igniteIdCache;

    private Map<String, Queue<UUID>> locationMap = new HashMap<>();

    private Map<String, UUID> idMap = new HashMap<>();

    MinionLookupService minionLookupService;

    UUID localNodeUUID;

    MinionInfo minionInfo;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        localNodeUUID = UUID.randomUUID();
        minionInfo = new MinionInfo();
        minionInfo.setId("blahId");
        minionInfo.setLocation("blahLocation");

        when(igniteIdCache.get(Mockito.any())).thenAnswer((Answer<UUID>) invocationOnMock -> idMap.get(invocationOnMock.getArgument(0)));
        when(igniteIdCache.remove(Mockito.any())).thenAnswer((Answer<Boolean>) invocationOnMock -> ( idMap.remove(invocationOnMock.getArgument(0)) != null ));
        doAnswer((Answer<UUID>) invocationOnMock -> idMap.put(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1))).
            when(igniteIdCache).put(Mockito.any(), Mockito.any());

        when(igniteLocationCache.get(Mockito.any())).thenAnswer((Answer<Queue<UUID>>) invocationOnMock -> locationMap.get(invocationOnMock.getArgument(0)));
        when(igniteLocationCache.remove(Mockito.any())).thenAnswer((Answer<Boolean>) invocationOnMock -> ( locationMap.remove(invocationOnMock.getArgument(0)) != null));
        doAnswer((Answer<Queue<UUID>>) invocationOnMock -> locationMap.put(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1))).
            when(igniteLocationCache).put(Mockito.any(), Mockito.any());

        when(ignite.getOrCreateCache(eq(MinionLookupServiceImpl.MINIONS_BY_ID))).thenReturn(igniteIdCache);
        when(ignite.getOrCreateCache(eq(MinionLookupServiceImpl.MINIONS_BY_LOCATION))).thenReturn(igniteLocationCache);
        when(ignite.cache(eq(MinionLookupServiceImpl.MINIONS_BY_ID))).thenReturn(igniteIdCache);
        when(ignite.cache(eq(MinionLookupServiceImpl.MINIONS_BY_ID))).thenReturn(igniteLocationCache);
        when(ignite.cluster()).thenReturn(igniteCluster);
        when(igniteCluster.localNode()).thenReturn(clusterNode);
        when(clusterNode.id()).thenReturn(localNodeUUID);

        
        minionLookupService = new MinionLookupServiceImpl(ignite);
    }
    

    @Test
    public void findGatewayNodeWithId() {
        generateMinions(3);

        UUID uuid = minionLookupService.findGatewayNodeWithId("minion1");
        assertEquals(localNodeUUID, uuid);

        uuid = minionLookupService.findGatewayNodeWithId("minion2");
        assertEquals(localNodeUUID, uuid);

        uuid = minionLookupService.findGatewayNodeWithId("bogus");
        assertNull(uuid);
    }

    @Test
    public void findGatewayNodeWithLocation() {
        generateMinions(3);

        Queue<UUID> uuids = minionLookupService.findGatewayNodeWithLocation("location");
        assertNotNull(uuids);
        assertEquals(3, uuids.size());

        assertEquals(localNodeUUID, uuids.stream().findFirst().get());

        uuids = minionLookupService.findGatewayNodeWithLocation("badLocation");
        assertNull(uuids);

    }

    @Test
    public void onMinionRemoved() {
        MinionInfo minionInfo1 = new MinionInfo();
        minionInfo1.setId("minion");
        minionInfo1.setLocation(("location"));

        minionLookupService.onMinionAdded(1, minionInfo1);

        assertNotNull(minionLookupService.findGatewayNodeWithId(minionInfo1.getId()));

        minionLookupService.onMinionRemoved(1, minionInfo1);

        assertNull(minionLookupService.findGatewayNodeWithId(minionInfo1.getId()));
    }

    private void generateMinions(int num) {
        for (int i=0;i<num;i++) {
            MinionInfo minionInfo1 = new MinionInfo();
            minionInfo1.setId("minion"+i);
            minionInfo1.setLocation(("location"));

            minionLookupService.onMinionAdded(i, minionInfo1);
        }
    }
}
