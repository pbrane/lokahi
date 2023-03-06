/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.horizon.alertservice.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.opennms.horizon.alertservice.api.AlertEntityListener;
import org.opennms.horizon.alertservice.api.AlertLifecycleListener;
import org.opennms.horizon.alertservice.db.entity.Alert;
import org.opennms.horizon.alertservice.db.entity.Memo;
import org.opennms.horizon.alertservice.db.repository.AlertRepository;
import org.opennms.horizon.alertservice.model.AlertSeverity;
import org.opennms.horizon.alertservice.utils.SystemProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlertLifecycleListenerManager implements AlertEntityListener {

    public static final String ALERT_SNAPSHOT_INTERVAL_MS_SYS_PROP = "org.opennms.alerts.snapshot.sync.ms";
    public static final long ALERT_SNAPSHOT_INTERVAL_MS = SystemProperties.getLong(ALERT_SNAPSHOT_INTERVAL_MS_SYS_PROP, TimeUnit.MINUTES.toMillis(2));

    private final Set<AlertLifecycleListener> listeners = Sets.newConcurrentHashSet();
    private Timer timer;

    @Autowired
    private AlertRepository alertRepository;

    @PostConstruct
    public void start() {
        timer = new Timer("AlertLifecycleListenerManager");
        // Use a fixed delay instead of a fixed interval so that snapshots are not constantly in progress
        // if they take a long time
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    doSnapshot();
                } catch (Exception e) {
                    log.error("Error while performing snapshot update.", e);
                }
            }
        }, 0, ALERT_SNAPSHOT_INTERVAL_MS);
    }

    @PreDestroy
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Transactional
    protected void doSnapshot() {
        if (listeners.size() < 1) {
            return;
        }

        final AtomicLong numAlerts = new AtomicLong(-1);
        final long systemMillisBeforeSnapshot = System.currentTimeMillis();
        final AtomicLong systemMillisAfterLoad = new AtomicLong(-1);
        try {
            forEachListener(AlertLifecycleListener::preHandleAlertSnapshot);
               // Load all of the alerts
               final List<Alert> allAlerts = alertRepository.findAll();
               numAlerts.set(allAlerts.size());
               // Save the timestamp after the load, so we can differentiate between how long it took
               // to load the alerts and how long it took to invoke the callbacks
               systemMillisAfterLoad.set(System.currentTimeMillis());
               forEachListener(l -> {
                   log.debug("Calling handleAlertSnapshot on listener: {}", l);
                   l.handleAlertSnapshot(allAlerts);
                   log.debug("Done calling listener.");
               });
        } finally {
            if (log.isDebugEnabled()) {
                final long now = System.currentTimeMillis();
                log.debug("Alert snapshot for {} alerts completed. Spent {}ms loading the alerts. " +
                                "Snapshot processing took a total of of {}ms.",
                        numAlerts.get(),
                        systemMillisAfterLoad.get() - systemMillisBeforeSnapshot,
                        now - systemMillisBeforeSnapshot);
            }
            forEachListener(AlertLifecycleListener::postHandleAlertSnapshot);
        }
    }

    public void onNewOrUpdatedAlert(Alert alert) {
        forEachListener(l -> l.handleNewOrUpdatedAlert(alert));
    }

    @Override
    public void onAlertArchived(Alert alert, String previousReductionKey) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onAlertDeleted(Alert alert) {
        forEachListener(l -> l.handleDeletedAlert(alert.getAlertId(), alert.getReductionKey()));
    }

    @Override
    public void onAlertCreated(Alert alert) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onAlertUpdatedWithReducedEvent(Alert alert) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onAlertAcknowledged(Alert alert, String previousAckUser, Date previousAckTime) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onAlertUnacknowledged(Alert alert, String previousAckUser, Date previousAckTime) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onAlertSeverityUpdated(Alert alert, AlertSeverity previousSeverity) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onStickyMemoUpdated(Alert alert, String previousBody, String previousAuthor, Date previousUpdated) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onReductionKeyMemoUpdated(Alert alert, String previousBody, String previousAuthor, Date previousUpdated) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onStickyMemoDeleted(Alert alert, Memo memo) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onLastAutomationTimeUpdated(Alert alert, Date previousLastAutomationTime) {
        onNewOrUpdatedAlert(alert);
    }

    @Override
    public void onRelatedAlertsUpdated(Alert alert, Set<Alert> previousRelatedAlerts) {
        onNewOrUpdatedAlert(alert);
    }

//    @Override
//    public void onTicketStateChanged(Alert alert, TroubleTicketState previousState) {
//        onNewOrUpdatedAlert(alert);
//    }

    private void forEachListener(Consumer<AlertLifecycleListener> callback) {
        for (AlertLifecycleListener listener : listeners) {
            try {
                callback.accept(listener);
            } catch (Exception e) {
                log.error("Error occurred while invoking listener: {}. Skipping.", listener, e);
            }
        }
    }

    public void onListenerRegistered(final AlertLifecycleListener listener, final Map<String,String> properties) {
        log.debug("onListenerRegistered: {} with properties: {}", listener, properties);
        if (listener!=null) { listeners.add(listener); }
    }

    public void setListener(final AlertLifecycleListener listener) {
        if (listener!=null) { onListenerRegistered(listener, null); }
    }

    public void onListenerUnregistered(final AlertLifecycleListener listener, final Map<String,String> properties) {
        log.debug("onListenerUnregistered: {} with properties: {}", listener, properties);
        if (listener!=null) { listeners.remove(listener); }
    }

}
