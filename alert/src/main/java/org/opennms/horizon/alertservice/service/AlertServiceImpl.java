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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.horizon.alertservice.api.AlertEntityNotifier;
import org.opennms.horizon.alertservice.api.AlertService;
import org.opennms.horizon.alertservice.db.entity.Alert;
import org.opennms.horizon.alertservice.db.entity.Memo;
import org.opennms.horizon.alertservice.db.repository.AlertRepository;
import org.opennms.horizon.alertservice.model.AlertDTO;
import org.opennms.horizon.alertservice.model.AlertSeverity;
import org.opennms.horizon.alertservice.utils.SystemProperties;
import org.opennms.horizon.events.proto.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    protected static final Integer THREADS = SystemProperties.getInteger("org.opennms.alertd.threads", 4);

    public static final String ALERT_RULES_NAME = "alert";

    protected static final String DEFAULT_USER = "admin";

    @Autowired
    AlertRepository alertRepository;

    @Autowired
    private AlertEntityNotifier alertEntityNotifier;

    @Autowired
    private AlertMapper alertMapper;

    @Override
    @Transactional
    public AlertDTO clearAlert(Alert alert, Date now) {
            log.info("Clearing alert with id: {} with current severity: {} at: {}", alert.getAlertId(), alert.getSeverity(), now);
            final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alert.getAlertId());
            if (maybeAlertInTrans.isEmpty()) {
                log.warn("Alert disappeared: {}. Skipping clear.", alert);
                return null;
            }
            Alert alertInTrans = maybeAlertInTrans.get();
            final AlertSeverity previousSeverity = alertInTrans.getSeverity();
            alertInTrans.setSeverity(AlertSeverity.CLEARED);
            updateAutomationTime(alertInTrans, now);
            alertRepository.save(alertInTrans);

            List<Alert> associatedAlerts = alert.getAssociatedAlerts().stream().map(alertAssociation -> alertAssociation.getRelatedAlertId()).collect(
                Collectors.toList());

            associatedAlerts.forEach(associatedAlert -> clearAlert(associatedAlert, now));

            alertEntityNotifier.didUpdateAlertSeverity(alertInTrans, previousSeverity);

            return alertMapper.alertToAlertDTO(alertInTrans);
    }

    @Override
    @Transactional
    public AlertDTO clearAlert(Long alertId, Date now) {
        return clearAlert(alertRepository.getById(alertId), now);
    }

    @Override
    @Transactional
    public AlertDTO deleteAlert(Alert alert) {
        return deleteAlert(alert.getAlertId());
    }

    @Override
    @Transactional
    public AlertDTO deleteAlert(Long id) {
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(id);

        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert with Id {}  disappeared. Skipping clear.", id);
            return null;
        }
        Alert alertInTrans = maybeAlertInTrans.get();

        log.info("Deleting alert with id: {} with severity: {}", alertInTrans.getAlertId(), alertInTrans.getSeverity());

        // If alert was in Situation, calculate notifications for the Situation
            Map<Alert, Set<Alert>> priorRelatedAlerts = new HashMap<>();
            if (alertInTrans.isPartOfSituation()) {
                for (Alert situation : alertInTrans.getRelatedSituations()) {
                    priorRelatedAlerts.put(situation, new HashSet<Alert>(situation.getRelatedAlerts()));
                }
            }
            alertRepository.delete(alertInTrans);
            // fire notifications after alert has been deleted
            for (Entry<Alert, Set<Alert>> entry : priorRelatedAlerts.entrySet()) {
                alertEntityNotifier.didUpdateRelatedAlerts(entry.getKey(), entry.getValue());
            }
            alertEntityNotifier.didDeleteAlert(alertInTrans);

            return alertMapper.alertToAlertDTO(alertInTrans);
    }

    @Override
    @Transactional
    public AlertDTO unclearAlert(Alert alert, Date now) {
            log.info("Un-clearing alert with id: {} at: {}", alert.getAlertId(), now);
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alert.getAlertId());
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alert);
            return null;
        }
        return doAlertUnclear(now, maybeAlertInTrans.get());
    }

    @Override
    @Transactional
    public AlertDTO unclearAlert(Long alertId, Date now) {
        log.info("Un-clearing alert with id: {} at: {}", alertId, now);
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alertId);
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alertId);
            return null;
        }
        return doAlertUnclear(now, maybeAlertInTrans.get());
    }

    private AlertDTO doAlertUnclear(Date now, Alert alert) {
        final AlertSeverity previousSeverity = alert.getSeverity();
        alert.setSeverity(alert.getLastEventSeverity());
        updateAutomationTime(alert, now);
        alertRepository.save(alert);
        alertEntityNotifier.didUpdateAlertSeverity(alert, previousSeverity);

        return alertMapper.alertToAlertDTO(alert);

    }

    @Override
    @Transactional
    public AlertDTO escalateAlert(Alert alert, Date now) {
            log.info("Escalating alert with id: {} at: {}", alert.getAlertId(), now);
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alert.getAlertId());
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alert);
            return null;
        }
        return doEscalateAlert(now, maybeAlertInTrans);
    }

    @Override
    @Transactional
    public AlertDTO escalateAlert(Long alertId, Date now) {
        log.info("Escalating alert with id: {} at: {}", alertId, now);
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alertId);
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alertId);
            return null;
        }
        return doEscalateAlert(now, maybeAlertInTrans);
    }

    private AlertDTO doEscalateAlert(Date now, Optional<Alert> maybeAlertInTrans) {
        Alert alertInTrans = maybeAlertInTrans.get();
        final AlertSeverity previousSeverity = alertInTrans.getSeverity();
        alertInTrans.setSeverity(AlertSeverity.escalate(previousSeverity));
        updateAutomationTime(alertInTrans, now);
        alertRepository.save(alertInTrans);
        alertEntityNotifier.didUpdateAlertSeverity(alertInTrans, previousSeverity);

        return alertMapper.alertToAlertDTO(alertInTrans);
    }

    @Override
    @Transactional
    public AlertDTO acknowledgeAlert(Alert alert, Date now, String userId) {
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alert.getAlertId());
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alert);
            return null;
        }

        return doAcknowledgeAlert(userId, maybeAlertInTrans.get());
    }

    @Override
    @Transactional
    public AlertDTO acknowledgeAlert(Long alertId, Date now, String userId) {
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alertId);
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alertId);
            return null;
        }

        return doAcknowledgeAlert(userId, maybeAlertInTrans.get());
    }

    private AlertDTO doAcknowledgeAlert(String userId, Alert alert) {
        alert.setAlertAckTime(new Date());
        alert.setAlertAckUser(userId);
        alertRepository.save(alert);

        return alertMapper.alertToAlertDTO(alert);
    }

    @Override
    @Transactional
    public AlertDTO unAcknowledgeAlert(Long alertId, Date now) {
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alertId);
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alertId);
            return null;
        }

        return doUnacknowledgeAlert(maybeAlertInTrans.get());
    }

    @Override
    @Transactional
    public AlertDTO unAcknowledgeAlert(Alert alert, Date now) {
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alert.getAlertId());
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alert);
            return null;
        }

        return doUnacknowledgeAlert(maybeAlertInTrans.get());
    }

    private AlertDTO doUnacknowledgeAlert(Alert alert) {
        alert.setAlertAckTime(null);
        alert.setAlertAckUser(null);
        alertRepository.save(alert);

        return alertMapper.alertToAlertDTO(alert);
    }

    @Override
    @Transactional
    public AlertDTO setSeverity(Alert alert, AlertSeverity severity, Date now) {
            log.info("Updating severity {} on alert with id: {}", severity, alert.getAlertId());
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alert.getAlertId());
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alert);
            return null;
        }
        return doSetSeverity(severity, now, maybeAlertInTrans.get());
    }

    @Override
    @Transactional
    public AlertDTO setSeverity(Long alertId, AlertSeverity severity, Date now) {
        log.info("Updating severity to {} on alert with id: {}", severity, alertId);
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alertId);
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping clear.", alertId);
            return null;
        }
        return doSetSeverity(severity, now, maybeAlertInTrans.get());
    }

    private AlertDTO doSetSeverity(AlertSeverity severity, Date now, Alert alertInTrans) {
        final AlertSeverity previousSeverity = alertInTrans.getSeverity();
        alertInTrans.setSeverity(severity);
        updateAutomationTime(alertInTrans, now);
        alertRepository.save(alertInTrans);
        alertEntityNotifier.didUpdateAlertSeverity(alertInTrans, previousSeverity);

        return alertMapper.alertToAlertDTO(alertInTrans);
    }

    @Override
    public List<AlertDTO> getAllAlerts(String tenantId) {
        List<Alert> alerts = alertRepository.findAll();

        List<AlertDTO> dtoAlertList =
            alerts
                .stream()
                .map(alert -> alertMapper.alertToAlertDTO(alert))
                .collect(Collectors.toList());

        return dtoAlertList;
    }

    @Override
    @Transactional
    public AlertDTO process(Event event) {
        Objects.requireNonNull(event, "Cannot create alert from null event.");

        log.debug("Processing Event: {}; nodeid: {}; ipaddr: {}", event.getUei(), event.getNodeId(), event.getIpAddress());

        Alert alert  = addOrReduceEventAsAlert(event);

        return alertMapper.alertToAlertDTO(alert);
    }

    @Override
    @Transactional
    public AlertDTO removeStickyMemo(long alertId) {
        log.info("Removing sticky memo on alert with id: {}", alertId);
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alertId);
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping sticky memo removal.", alertId);
            return null;
        }

        Alert targetAlert = maybeAlertInTrans.get();

        if (targetAlert.getStickyMemo() != null) {
            targetAlert.setStickyMemo(null);
            alertRepository.save(targetAlert);
        }

        return alertMapper.alertToAlertDTO(targetAlert);
    }

    @Override
    public AlertDTO updateStickyMemo(Long alertId, String body) {
        log.info("Updating sticky memo on alert with id: {}", alertId);
        final Optional<Alert> maybeAlertInTrans = alertRepository.findById(alertId);
        if (maybeAlertInTrans.isEmpty()) {
            log.warn("Alert disappeared: {}. Skipping sticky memo removal.", alertId);
            return null;
        }

        Alert targetAlert = maybeAlertInTrans.get();

        if (targetAlert.getStickyMemo() == null) {
            Memo memo = new Memo();
            memo.setBody(body);
            memo.setUpdated(new Date());
            memo.setCreated(new Date());
            targetAlert.setStickyMemo(memo);
            alertRepository.save(targetAlert);
        }

        return alertMapper.alertToAlertDTO(targetAlert);
    }

    protected Alert addOrReduceEventAsAlert(Event event) throws IllegalStateException {

//        String reductionKey = String.format("%s:%d:%s", event.getUei(), event.getNodeId(), "TODO:Need tenant id");
        String reductionKey = event.getAlertData().getReductionKey();
        String clearKey = event.getAlertData().getClearKey();

        log.debug("Looking for existing clearKey: {}", clearKey);

        Alert alert = alertRepository.findByReductionKey(clearKey);

        if (alert == null) {
            log.debug("looking for existing reductionKey: {}", reductionKey);
            alert = alertRepository.findByReductionKey(reductionKey);
        }

        if (alert == null ) {
            log.debug("reductionKey or clearKey not found, instantiating new alert");

            alert = createNewAlert(event);

            alertEntityNotifier.didCreateAlert(alert);
        } else {
            log.debug("reductionKey or clearKey found, reducing event to existing alert: {}", alert.getAlertId());

            alert.incrementCount();

            alertEntityNotifier.didUpdateAlertWithReducedEvent(alert);
        }

        alertRepository.save(alert);

        return alert;
    }

    private static void updateAutomationTime(Alert alert, Date now) {
        if (alert.getFirstAutomationTime() == null) {
            alert.setFirstAutomationTime(now);
        }
        alert.setLastAutomationTime(now);
    }

    public void setAlertEntityNotifier(AlertEntityNotifier alertEntityNotifier) {
        this.alertEntityNotifier = alertEntityNotifier;
    }

    private Alert createNewAlert(Event event) {
        Date now = new Date();
        Alert alert = new Alert();

        alert.setAlertType(1);
        alert.setClearKey(event.getAlertData().getClearKey());
        alert.setCounter(1);
        alert.setLastEventTime(now);
        alert.setLastAutomationTime(now);
        //TODO:Not sure this is a robust mapping. Maybe merge the two enums?
        alert.setSeverity(AlertSeverity.get(event.getEventSeverity().getNumber()));
        alert.setLastEventSeverity(alert.getSeverity());
        alert.setReductionKey(event.getAlertData().getReductionKey());
        alert.setEventUei(event.getUei() + event.getNodeId());
        alert.setX733ProbableCause(1);
        alert.setDetails(new HashMap<>());
        alert.setRelatedAlerts(new HashSet<>());

        // Situations are denoted by the existance of related-reductionKeys
//        alert.setRelatedAlerts(getRelatedAlerts(event.getParmCollection()), event.getTime());

//        alert.setDescription(e.getEventDescr());
//        alert.setDistPoller(e.getDistPoller());
//        alert.setFirstEventTime(e.getEventTime());
//        alert.setIfIndex(e.getIfIndex());
//        alert.setIpAddr(e.getIpAddr());

//        alert.setLastEvent(e);
//        alert.setLogMsg(e.getEventLogMsg());
//        alert.setMouseOverText(e.getEventMouseOverText());
//        alert.setNode(e.getNode());
//        alert.setOperInstruct(e.getEventOperInstruct());

//        alert.setServiceType(e.getServiceType());
//        alert.setSuppressedUntil(e.getEventTime()); //UI requires this be set
//        alert.setSuppressedTime(e.getEventTime()); // UI requires this be set

//        if (event.getAlertData().getManagedObject() != null) {
//            alert.setManagedObjectType(event.getAlertData().getManagedObject().getType());
//        }
//        e.setAlert(alert);
        
        return alert;
    }

}
