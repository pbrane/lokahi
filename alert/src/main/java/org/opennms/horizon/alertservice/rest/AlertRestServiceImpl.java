/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.horizon.alertservice.rest;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.opennms.horizon.alertservice.api.AlertService;
import org.opennms.horizon.alertservice.model.AlertDTO;
import org.opennms.horizon.alertservice.model.AlertSeverity;
import org.opennms.horizon.alertservice.rest.support.MultivaluedMapImpl;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@Setter
@Slf4j
@RestController
@RequestMapping(path = "/alerts")
public class AlertRestServiceImpl  {

    @Autowired
    private AlertService alertService;

    protected WebApplicationException getException(final Status status, String msg, String... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        log.error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

//========================================
// Interface
//========================================


    @GetMapping(path = "list", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
//    @RolesAllowed({ "admin" })
    @ApiResponse(
            description = "Retrieve the list of alerts"
    )
    public ResponseEntity<AlertCollectionDTO> getAlerts(/*@Context final SecurityContext securityContext, @Context final UriInfo uriInfo*/) {
        // replace the next line with @RolesAllowed("")
        //SecurityHelper.assertUserReadCredentials(securityContext);

            List<AlertDTO> dtoAlertList = alertService.getAllAlerts("TODO:MMF need a tenant id!");

            AlertCollectionDTO alertsCollection = new AlertCollectionDTO(dtoAlertList);
            alertsCollection.setTotalCount(dtoAlertList.size());

            return ResponseEntity.ok(alertsCollection);
    }
    
    @PostMapping(path="/clear/{id}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertDTO> clearAlert(@PathVariable Long id) {

        return ResponseEntity.ok(alertService.clearAlert(id, new Date()));
    }

    @PostMapping(path="/unclear/{id}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertDTO> unClearAlert(@PathVariable Long id) {

        return ResponseEntity.ok(alertService.unclearAlert(id, new Date()));
    }

    @PostMapping(path="/ack/{id}/{userId}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertDTO> acknowledgeAlert(@PathVariable Long id, @PathVariable String userId) {

        return ResponseEntity.ok(alertService.acknowledgeAlert(id, new Date(), userId));
    }

    @PostMapping(path="/unAck/{id}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertDTO> acknowledgeAlert(@PathVariable Long id) {

        return ResponseEntity.ok(alertService.unAcknowledgeAlert(id, new Date()));
    }

    @DeleteMapping(path="/delete/{id}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertDTO> deleteAlert(@PathVariable Long id) {

        return ResponseEntity.ok(alertService.deleteAlert(id));
    }

    @PostMapping(path="/escalate/{id}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertDTO> escalateAlert(@PathVariable Long id) {

        return ResponseEntity.ok(alertService.escalateAlert(id, new Date()));
    }

    @PostMapping(path="/severity/{id}/{label}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertDTO> escalateAlert(@PathVariable Long id, @PathVariable String label) {

        return ResponseEntity.ok(alertService.setSeverity(id, AlertSeverity.get(label), new Date()));
    }

    @PutMapping(path = "memo/{alertId}",  consumes = MediaType.APPLICATION_JSON, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
//    @RolesAllowed({ "admin" })
    @Transactional
    public ResponseEntity updateMemo(/*@Context final SecurityContext securityContext,*/ @PathVariable final Long alertId, @RequestBody final MultivaluedMapImpl params) {
        // replace the next two lines with @RolesAllowed("")
//        final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
//        SecurityHelper.assertUserEditCredentials(securityContext, user);

            final String body = params.getFirst("body");
            if (body == null) {
                throw getException(Status.BAD_REQUEST, "Body cannot be null.");
            }

            return ResponseEntity.ok(alertService.updateStickyMemo(alertId, body));
    }

    @RolesAllowed({ "admin" })
    @ApiResponse(
            description = "Update the journal for an Alert"
    )
    @PutMapping(path = "journal/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity updateJournal(@Context final SecurityContext securityContext, @PathVariable final Long alertId, final MultivaluedMapImpl params) {
            final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
            // SecurityHelper.assertUserEditCredentials(securityContext, user);
            final String body = params.getFirst("body");
            if (body == null) throw getException(Status.BAD_REQUEST, "Body cannot be null.");
            //alertRepository.updateReductionKeyMemo(alertId, body, user); // TODO doing anything??
            return ResponseEntity.noContent().build();
    }

//    @RolesAllowed({ "admin" })
    @ApiResponse(
        description = "Remove the memo for an Alert"
    )

    @DeleteMapping(path = "removeMemo/{alertId}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity removeMemo(/*@Context final SecurityContext securityContext, */@PathVariable final Long alertId) {

        return ResponseEntity.ok(alertService.removeStickyMemo(alertId));

    }
}