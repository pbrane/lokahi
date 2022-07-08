/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.minion.rest.internal;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.opennms.horizon.db.dao.api.AlarmDao;
import org.opennms.horizon.db.dao.api.MinionDao;
import org.opennms.horizon.db.dao.api.SessionUtils;
import org.opennms.horizon.db.model.OnmsAlarm;
import org.opennms.horizon.db.model.OnmsMinion;
import org.opennms.horizon.db.model.dto.AlarmCollectionDTO;
import org.opennms.horizon.db.model.dto.AlarmDTO;
import org.opennms.horizon.db.model.dto.MinionCollectionDTO;
import org.opennms.horizon.db.model.dto.MinionDTO;
import org.opennms.horizon.db.model.mapper.AlarmMapper;
import org.opennms.horizon.db.model.mapper.MinionMapper;
import org.opennms.netmgt.minion.rest.MinionRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Basic Web Service using REST for {@link OnmsMinion} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Path("minions")
public class MinionRestServiceImpl implements MinionRestService {

    private static final Logger LOG = LoggerFactory.getLogger(MinionRestServiceImpl.class);
    private static final String PROVISIONING_FOREIGN_SOURCE_PATTERN = System.getProperty("opennms.minion.provisioning.foreignSourcePattern", "Minions");

    private MinionDao minionDao;
    private MinionMapper m_minionMapper;
    private SessionUtils sessionUtils;

    protected MinionDao getDao() {
        return minionDao;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }
    public void setMinionDao(MinionDao minionDao) {
        this.minionDao = minionDao;
    }
    public void setMinionMapper(MinionMapper m_minionMapper) {
        this.m_minionMapper = m_minionMapper;
    }

    @GET
    @Path("list")
    @RolesAllowed({ "admin" })
    @ApiResponse(description = "Get minions")
    public Response getMinions(@Context final SecurityContext securityContext, final UriInfo uriInfo) {
        return this.sessionUtils.withReadOnlyTransaction(() -> {

            List<OnmsMinion> matchingMinions = this.minionDao.findAll(0, Integer.MAX_VALUE);

            List<MinionDTO> dtoMinionList =
                    matchingMinions
                            .stream()
                            .map(this.m_minionMapper::minionToMinionDTO)
                            .collect(Collectors.toList());

            MinionCollectionDTO minionCollection = new MinionCollectionDTO(dtoMinionList);
            minionCollection.setTotalCount(dtoMinionList.size());

            return Response.status(Status.OK).entity(minionCollection).build();
        });
    }
}
