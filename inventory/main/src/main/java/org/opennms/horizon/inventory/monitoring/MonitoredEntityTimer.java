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
package org.opennms.horizon.inventory.monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.inventory.repository.MonitoringLocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoredEntityTimer {

    private final MonitoredEntityService monitoredEntityService;
    private final MonitoringLocationRepository monitoringLocationRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${monitor.interval.seconds}")
    private long interval;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        log.info("Scheduler is calling ...");
        scheduler.scheduleAtFixedRate(this::republish, 0, interval, TimeUnit.SECONDS);
    }

    private void republish() {

        // Disabling this, no need to republish tasks every few mins.
        /*        monitoringLocationRepository
        .findAll()
        .forEach(obj -> monitoredEntityService.publishTaskSet(obj.getTenantId(), obj.getId(), new HashMap<>()));*/
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        scheduler.shutdown();
        log.info("Scheduler stop successfully ...");
    }
}
