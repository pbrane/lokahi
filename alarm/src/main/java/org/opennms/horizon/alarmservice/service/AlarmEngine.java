/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
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

package org.opennms.horizon.alarmservice.service;

import io.grpc.Context;
import lombok.RequiredArgsConstructor;
import org.opennms.horizon.alarms.proto.Alarm;
import org.opennms.horizon.alarmservice.api.AlarmLifecyleListener;
import org.opennms.horizon.alarmservice.api.AlarmService;
import org.opennms.horizon.alarmservice.db.repository.AlarmRepository;
import org.opennms.horizon.shared.constants.GrpcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A simple engine that stores alarms in memory and periodically scans the list to performs actions (i.e. delete if older than X).
 *
 * This is a very limited and temporary solution, to which we may re-introduce Drools or some alternative to perform.
 */
@Component
@RequiredArgsConstructor
public class AlarmEngine implements AlarmLifecyleListener {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmEngine.class);

    private final AlarmService alarmService;
    private final AlarmMapper alarmMapper;
    private final AlarmRepository alarmRepository;

    private final AlarmListenerRegistry alarmEntityNotifier;
    private final Map<String, Map<String, Alarm>> alarmsByReductionKeyByTenantId = new ConcurrentHashMap<>();
    private final Timer nextTimer = new Timer();

    @PostConstruct
    @Transactional
    public void init() {
        alarmEntityNotifier.addListener(this);
        nextTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    tick();
                } catch (RuntimeException e) {
                    LOG.error("Error happened in tick. Keeping on ticking...", e);
                }
            }
        }, TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5));
        alarmRepository.findAll().forEach(a -> handleNewOrUpdatedAlarm(alarmMapper.toProto(a)));
    }

    @PreDestroy
    public void destroy() {
        alarmEntityNotifier.removeListener(this);
        nextTimer.cancel();
    }

    @Override
    public synchronized void handleNewOrUpdatedAlarm(Alarm alarm) {
        alarmsByReductionKeyByTenantId.compute(alarm.getTenantId(), (k,v) -> {
            Map<String, Alarm> alarmsByReductionKey = (v == null) ? new ConcurrentHashMap<>() : v;
            alarmsByReductionKey.put(alarm.getReductionKey(), alarm);
            return alarmsByReductionKey;
        });
    }

    @Override
    public synchronized void handleDeletedAlarm(Alarm alarm) {
        Map<String,Alarm> alarmsByReductionKey = alarmsByReductionKeyByTenantId.getOrDefault(alarm.getTenantId(), new HashMap<>());
        alarmsByReductionKey.compute(alarm.getReductionKey(), (k,v) -> {
                if (v == null) {
                    LOG.error("Received delete for alarm that was not present in the cache. Alarm: {}", alarm);
                }
                return null;
            });
        if (alarmsByReductionKey.isEmpty()) {
            // Stop tracking tenant
            alarmsByReductionKeyByTenantId.remove(alarm.getTenantId());
        }
    }

    private synchronized void tick() {
        LOG.info("Tick with: {}", alarmsByReductionKeyByTenantId);
        // Delete alarms more than 1 hour old
        alarmsByReductionKeyByTenantId.forEach((tenantId, alarmsByReductionKey) -> {
            Context.current().withValue(GrpcConstants.TENANT_ID_CONTEXT_KEY, tenantId).run(()-> {
                alarmsByReductionKey.values().stream()
                    .filter(a -> a.getLastUpdateTimeMs() < (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)))
                    .forEach(this::deleteAlarm);
            });
        });
    }

    private void deleteAlarm(Alarm alarm) {
        LOG.info("Delete alarm with reduction key: {} for tenant id: {}", alarm.getReductionKey(), alarm.getTenantId());
        try {
            alarmService.deleteAlarm(alarm);
        } catch (EmptyResultDataAccessException ex) {
            LOG.warn("Could not find alarm alarm with reduction key: {} for tenant id: {}. Will stop tracking.", alarm.getReductionKey(), alarm.getTenantId());
        }
        // We expect the delete call to the AlarmService to issue a callback to our listener, which will remove the entry from the map
    }
}
