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

package org.opennms.horizon.alertservice.drools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.opennms.horizon.alertservice.api.AlertLifecycleListener;
import org.opennms.horizon.alertservice.api.AlertService;
import org.opennms.horizon.alertservice.db.entity.Alert;
import org.opennms.horizon.alertservice.db.entity.AlertAssociation;
import org.opennms.horizon.alertservice.db.repository.AlertRepository;
import org.opennms.horizon.alertservice.service.AlertServiceImpl;
import org.opennms.horizon.alertservice.utils.SystemProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.swrve.ratelimitedlogger.RateLimitedLog;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class maintains the Drools context used to manage the lifecycle of the alerts.
 *
 * We drive the facts in the Drools context using callbacks provided by the {@link AlertLifecycleListener}.
 *
 * Atomic actions are used to update facts in working memory.
 *
 * @author jwhite
 */
@Slf4j
@Component
@Setter
public class DroolsAlertContext extends ManagedDroolsContext implements AlertLifecycleListener {

    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(log)
            .maxRate(5)
            .every(Duration.ofSeconds(30))
            .build();

    private static final long MAX_NUM_ACTIONS_IN_FLIGHT = SystemProperties.getLong(
            "org.opennms.netmgt.alertd.drools.max_num_actions_in_flight", 5000);

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository alertRepository;

    private final AlertCallbackStateTracker stateTracker = new AlertCallbackStateTracker();

    private final Map<Long, AlertAndFact> alertsById = new HashMap<>();

    private final Map<Long, Map<Long, AlertAssociationAndFact>> alertAssociationById = new HashMap<>();

    private final CountDownLatch seedSubmittedLatch = new CountDownLatch(1);

    private final AtomicLong atomicActionsInFlight = new AtomicLong(-1);
    private final AtomicLong numAlertsFromLastSnapshot = new AtomicLong(-1);
    private final AtomicLong numSituationsFromLastSnapshot = new AtomicLong(-1);
    private final Meter atomicActionsDropped = new Meter();
    private final Meter atomicActionsQueued = new Meter();

    public DroolsAlertContext() {
        this(getRulesResourceNames());
    }

    public DroolsAlertContext(List<String> rulesResourceNames) {
        super(rulesResourceNames, AlertServiceImpl.ALERT_RULES_NAME, "DroolsAlertContext");
        setOnNewKiewSessionCallback(kieSession -> {
            kieSession.setGlobal("alertService", alertService);

            // Rebuild the fact handle maps
            alertsById.clear();
            alertAssociationById.clear();
            for (FactHandle fact : kieSession.getFactHandles()) {
                final Object objForFact = kieSession.getObject(fact);
                if (objForFact instanceof Alert) {
                    final Alert alertInSession = (Alert)objForFact;
                    alertsById.put(alertInSession.getAlertId(), new AlertAndFact(alertInSession, fact));
                } else if (objForFact instanceof AlertAssociation) {
                    final AlertAssociation associationInSession = (AlertAssociation)objForFact;
                    final Long situationId = associationInSession.getSituationAlertId().getAlertId();
                    final Long alertId = associationInSession.getRelatedAlertId().getAlertId();
                    final Map<Long, AlertAssociationAndFact> associationFacts = alertAssociationById.computeIfAbsent(situationId, (sid) -> new HashMap<>());
                    associationFacts.put(alertId, new AlertAssociationAndFact(associationInSession, fact));
                }
            }

            // Reset metrics
            atomicActionsInFlight.set(0L);
            numAlertsFromLastSnapshot.set(-1L);
            numSituationsFromLastSnapshot.set(-1L);
        });

        // Register metrics
        getMetrics().register("atomicActionsInFlight", (Gauge<Long>) atomicActionsInFlight::get);
        getMetrics().register("numAlertsFromLastSnapshot", (Gauge<Long>) numAlertsFromLastSnapshot::get);
        getMetrics().register("numSituationsFromLastSnapshot", (Gauge<Long>) numSituationsFromLastSnapshot::get);
        getMetrics().register("atomicActionsDropped", atomicActionsDropped);
        getMetrics().register("atomicActionsQueued", atomicActionsQueued);
    }

//    public static File getDefaultRulesFolder() {
//        // FIXME: OOPS: Ugly
//        try {
//            Path rulesFolder = Files.createTempDirectory("rules");
//            Bundle bundle = FrameworkUtil.getBundle(DroolsAlertContext.class);
//            copy(bundle.getResource("rules/alertd.drl"), rulesFolder.resolve("alertd.drl"));
//            copy(bundle.getResource("rules/situations.drl"), rulesFolder.resolve("situations.drl"));
//            return rulesFolder.toFile();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static List<String> getRulesResourceNames() {
        return Arrays.asList("/rules/alert.drl", "/rules/situations.drl");
    }

    public static void copy(URL url, final Path target) throws IOException {
        try (InputStream in = url.openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    @Transactional
    public void onStart() {
        final Thread seedThread = new Thread(() -> {
            // Seed the engine with the current set of alerts asynchronously
            // We do this async since we don't want to block the whole system from starting up
            // while we wait on the database (particularly for systems with large amounts of alerts)
            try {
                preHandleAlertSnapshot();
                    log.info("Loading all alerts to seed Drools context.");
                    final List<Alert> allAlerts = alertRepository.findAll();
                    log.info("Done loading {} alerts.", allAlerts.size());
                    // Leverage the existing snapshot processing function to see the engine
                    handleAlertSnapshot(allAlerts);
                    // Seed was submitted as an atomic action
                    seedSubmittedLatch.countDown();
            } finally {
                postHandleAlertSnapshot();
            }
        });
        seedThread.setName("DroolAlertContext-InitialSeed");
        seedThread.start();
    }

    @Override
    public void preHandleAlertSnapshot() {
        // Start tracking alert callbacks via the state tracker
        stateTracker.startTrackingAlerts();
    }

    /**
     * When running in the context of a transaction, execute the given action
     * when the transaction is complete and has been successfully committed.
     *
     * If the transaction does not complete successfully, log a warning and drop the action.
     *
     * If we're not currently in a transaction, execute the action immediately.
     *
     * @param atomicAction action to consider
     */
    private void executeAtomicallyWhenTransactionComplete(KieSession.AtomicAction atomicAction) {
        // FIXME: OOPS
        throw new RuntimeException("FIXME: OOPS");
//
//        if (TransactionSynchronizationManager.isSynchronizationActive()) {
//            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//                @Override
//                public void afterCompletion(int status) {
//                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
//                        RATE_LIMITED_LOGGER.warn("A database transaction did not complete successfully. " +
//                                "The alerts facts in the session may be out of sync until the next snapshot.");
//                        return;
//                    }
//                    submitOrRun(atomicAction);
//                }
//            });
//        } else {
//            submitOrRun(atomicAction);
//        }
    }

    private void submitOrRun(KieSession.AtomicAction atomicAction) {
        if (fireThreadId.get() == Thread.currentThread().getId()) {
            // This is the fire thread! Let's execute the action immediately instead of deferring it.
            atomicAction.execute(getKieSession());
        } else {
            // Submit the action for execution
            // Track the number of atomic actions waiting to be executed
            final long numActionsInFlight = atomicActionsInFlight.incrementAndGet();
            if (numActionsInFlight > MAX_NUM_ACTIONS_IN_FLIGHT) {
                RATE_LIMITED_LOGGER.error("Dropping action - number of actions in flight exceed {}! " +
                        "Alerts in Drools context will not match those in the database until the next successful sync.", MAX_NUM_ACTIONS_IN_FLIGHT);
                atomicActionsDropped.mark();
                atomicActionsInFlight.decrementAndGet();
                return;
            }
            getKieSession().submit(kieSession -> {
                atomicAction.execute(kieSession);
                atomicActionsInFlight.decrementAndGet();
            });
            atomicActionsQueued.mark();
        }
    }

    @Override
    public void handleAlertSnapshot(List<Alert> alerts) {
        if (!isStarted()) {
            log.debug("Ignoring alert snapshot. Drools session is stopped.");
            return;
        }

        log.debug("Handling snapshot for {} alerts.", alerts.size());
        final Map<Long, Alert> alertsInDbById = alerts.stream()
                .filter(a -> a.getAlertId() != null)
                .collect(Collectors.toMap(Alert::getAlertId, a -> a));

        // Eagerly initialize the alerts
        for (Alert alert : alerts) {
            eagerlyInitializeAlert(alert);
        }

        // Track some stats
        final long numSituations = alerts.stream().filter(Alert::isSituation).count();
        numAlertsFromLastSnapshot.set(alerts.size() - numSituations);
        numSituationsFromLastSnapshot.set(numSituations);

        submitOrRun(kieSession -> {
            final Set<Long> alertIdsInDb = alertsInDbById.keySet();
            final Set<Long> alertIdsInWorkingMem = alertsById.keySet();

            final Set<Long> alertIdsToAdd = Sets.difference(alertIdsInDb, alertIdsInWorkingMem).stream()
                    // The snapshot contains an alert which we don't have in working memory.
                    // It is possible that the alert was in fact deleted some time after the
                    // snapshot was processed. We should only add it, if we did not explicitly
                    // delete the alert after the snapshot was taken.
                    .filter(alertId -> !stateTracker.wasAlertWithIdDeleted(alertId))
                    .collect(Collectors.toSet());
            final Set<Long> alertIdsToRemove = Sets.difference(alertIdsInWorkingMem, alertIdsInDb).stream()
                    // We have an alert in working memory that is not contained in the snapshot.
                    // Only remove it from memory if the fact we have dates before the snapshot.
                    .filter(alertId -> !stateTracker.wasAlertWithIdUpdated(alertId))
                    .collect(Collectors.toSet());
            final Set<Long> alertIdsToUpdate = Sets.intersection(alertIdsInWorkingMem, alertIdsInDb).stream()
                    // This stream contains the set of all alerts which are both in the snapshot
                    // and in working memory
                    .filter(alertId -> {
                        final AlertAndFact alertAndFact = alertsById.get(alertId);
                        // Don't bother updating the alert in memory if the fact we have is more recent than the snapshot
                        if (stateTracker.wasAlertWithIdUpdated(alertId)) {
                            return false;
                        }
                        final Alert alertInMem = alertAndFact.getAlert();
                        final Alert alertInDb = alertsInDbById.get(alertId);
                        // Only update the alerts if they are different
                        return shouldUpdateAlertForSnapshot(alertInMem, alertInDb);
                    })
                    .collect(Collectors.toSet());

            // Log details that help explain what actions are being performed, if any
            if (log.isDebugEnabled()) {
                if (!alertIdsToAdd.isEmpty() || !alertIdsToRemove.isEmpty() || !alertIdsToUpdate.isEmpty()) {
                    log.debug("Adding {} alerts, removing {} alerts and updating {} alerts for snapshot.",
                            alertIdsToAdd.size(), alertIdsToRemove.size(), alertIdsToUpdate.size());
                } else {
                    log.debug("No actions to perform for alert snapshot.");
                }
                // When TRACE is enabled, include diagnostic information to help explain why
                // the alerts are being updated
                if (log.isTraceEnabled()) {
                    for (Long alertIdToUpdate : alertIdsToUpdate) {
                        log.trace("Updating alert with id={}. Alert from DB: {} vs Alert from memory: {}",
                                alertIdToUpdate,
                                alertsInDbById.get(alertIdToUpdate),
                                alertsById.get(alertIdToUpdate));
                    }
                }
            }

            for (Long alertIdToRemove : alertIdsToRemove) {
                handleDeletedAlertForAtomic(kieSession, alertIdToRemove, alertsById.get(alertIdToRemove).getAlert().getReductionKey());
            }

            final Set<Alert> alertsToUpdate = Sets.union(alertIdsToAdd, alertIdsToUpdate).stream()
                    .map(alertsInDbById::get)
                    .collect(Collectors.toSet());
//            for (Alert alert : alertsToUpdate) {
//                handleNewOrUpdatedAlertForAtomic(kieSession, alert, acksByRefId.get(alert.getId()));
//            }

            stateTracker.resetStateAndStopTrackingAlerts();
            log.debug("Done handling snapshot.");
        });
    }

    @Override
    public void postHandleAlertSnapshot() {
        // pass
    }

    /**
     * Used to determine if an alert that is presently in the working memory should be updated
     * with the given alert, when handling alert snapshots.
     *
     * @param alertInMem the alert that is currently in the working memory
     * @param alertInDb the alert that is currently in the database
     * @return true if the alert in the working memory should be updated, false otherwise
     */
    protected static boolean shouldUpdateAlertForSnapshot(Alert alertInMem, Alert alertInDb) {
        return !Objects.equals(alertInMem.getLastEventTime(), alertInDb.getLastEventTime());
    }

    @Override
    public void handleNewOrUpdatedAlert(Alert alert) {
        if (!isStarted()) {
            log.debug("Ignoring new/updated alert. Drools session is stopped.");
            return;
        }
        eagerlyInitializeAlert(alert);
    }


    private void eagerlyInitializeAlert(Alert alert) {
        // Initialize any related objects that are needed for rule execution
        Hibernate.initialize(alert.getAssociatedAlerts());
//        if (alert.getLastEvent() != null) {
//            // The last event may be null in unit tests
//            try {
//                Hibernate.initialize(alert.getLastEvent().getEventParameters());
//            } catch (ObjectNotFoundException ex) {
//                // This may be triggered if the event attached to the alert entity is already gone
//                alert.setLastEvent(null);
//            }
//        }
    }

    private void handleNewOrUpdatedAlertForAtomic(KieSession kieSession, Alert alert) {
        final AlertAndFact alertAndFact = alertsById.get(alert.getAlertId());
        if (alertAndFact == null) {
            log.debug("Inserting alert into session: {}", alert);
            final FactHandle fact = kieSession.insert(alert);
            alertsById.put(alert.getAlertId(), new AlertAndFact(alert, fact));
        } else {
            // Updating the fact doesn't always give us to expected results so we resort to deleting it
            // and adding it again instead
            log.trace("Deleting alert from session (for re-insertion): {}", alert);
            kieSession.delete(alertAndFact.getFact());
            // Reinsert
            log.trace("Re-inserting alert into session: {}", alert);
            final FactHandle fact = kieSession.insert(alert);
            alertsById.put(alert.getAlertId(), new AlertAndFact(alert, fact));
        }

        if (alert.isSituation()) {
            final Alert situation = alert;
            final Map<Long, AlertAssociationAndFact> associationFacts = alertAssociationById.computeIfAbsent(situation.getAlertId(), (sid) -> new HashMap<>());
            for (AlertAssociation association : situation.getAssociatedAlerts()) {
                Long alertId = association.getRelatedAlertId().getAlertId();
                AlertAssociationAndFact associationFact = associationFacts.get(alertId);
                if (associationFact == null) {
                    log.debug("Inserting alert association into session: {}", association);
                    final FactHandle fact = kieSession.insert(association);
                    associationFacts.put(alertId, new AlertAssociationAndFact(association, fact));
                } else {
                    FactHandle fact = associationFact.getFact();
                    log.trace("Updating alert association in session: {}", associationFact);
                    kieSession.update(fact, association);
                    associationFacts.put(alertId, new AlertAssociationAndFact(association, fact));
                }
            }
            // Remove Fact for any Alerts no longer in the Situation
            Set<Long> deletedAlertIds = associationFacts.values().stream()
                    .map(fact -> fact.getAlertAssociation().getRelatedAlertId().getAlertId())
                    .filter(alertId -> !situation.getRelatedAlertIds().contains(alertId))
                    .collect(Collectors.toSet());
            deletedAlertIds.forEach(alertId -> {
                final AlertAssociationAndFact associationAndFact = associationFacts.remove(alertId);
                if (associationAndFact != null) {
                    log.debug("Deleting AlertAssociationAndFact from session: {}", associationAndFact.getAlertAssociation());
                    kieSession.delete(associationAndFact.getFact());
                }
            });
        }
    }

    @Override
    public void handleDeletedAlert(long alertId, String reductionKey) {
        if (!isStarted()) {
            log.debug("Ignoring deleted alert. Drools session is stopped.");
            return;
        }

        executeAtomicallyWhenTransactionComplete(kieSession -> {
            handleDeletedAlertForAtomic(kieSession, alertId, reductionKey);
            stateTracker.trackDeletedAlert(alertId, reductionKey);
        });
    }

    private void handleDeletedAlertForAtomic(KieSession kieSession, long alertId, String reductionKey) {
        final AlertAndFact alertAndFact = alertsById.remove(alertId);
        if (alertAndFact != null) {
            log.debug("Deleting alert from session: {}", alertAndFact.getAlert());
            kieSession.delete(alertAndFact.getFact());
        }

        final Map<Long, AlertAssociationAndFact> associationFacts = alertAssociationById.remove(alertId);
        if (associationFacts == null) {
            return;
        }
        for (Long association : associationFacts.keySet()) {
            AlertAssociationAndFact associationFact = associationFacts.get(association);
            if (associationFact != null) {
                log.debug("Deleting association from session: {}", associationFact.getAlertAssociation());
                kieSession.delete(associationFact.getFact());
            }
        }
    }

    @VisibleForTesting
    public void waitForInitialSeedToBeSubmitted() throws InterruptedException {
        seedSubmittedLatch.await();
    }
}
