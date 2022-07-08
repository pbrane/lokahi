/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.horizon.db.dao.impl;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.dao.api.MinionDao;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;
import org.opennms.horizon.db.model.OnmsMinion;
import org.opennms.horizon.db.model.OnmsMonitoringSystem;

/**
 * <p>LinkStateDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MinionDaoHibernate extends AbstractDaoHibernate<OnmsMinion, String> implements MinionDao {
    public MinionDaoHibernate(EntityManagerHolder persistenceContextHolder) {
        super(persistenceContextHolder, OnmsMinion.class);
    }

    @Override
    public List<OnmsMinion> findAll(final Integer offset, final Integer limit) {
        TypedQuery<OnmsMinion> query = getEntityManager().createQuery("SELECT m.* from monitoringSystems m where type = :type LIMIT :limit OFFSET :offset", OnmsMinion.class);
        query.setParameter("type", OnmsMonitoringSystem.TYPE_MINION);
        query.setParameter("offset", offset);
        query.setParameter("limit", limit);

        return query.getResultList();
    }

    @Override
    public OnmsMinion findById(final String id) {
        TypedQuery<OnmsMinion> query = getEntityManager().createQuery("SELECT m.* from monitoringSystems m where type = :type and m.id = :id", OnmsMinion.class);
        query.setParameter("type", OnmsMonitoringSystem.TYPE_MINION);
        query.setParameter("id", id);
        OnmsMinion minion = null;
        try {
            minion = query.getSingleResult();
        } catch (NoResultException e) {
            // nothing to do
        }
        return minion;
    }
    
    @Override
    public Collection<OnmsMinion> findByLocation(final String locationName) {
        TypedQuery<OnmsMinion> query = getEntityManager().createQuery("SELECT m.* from monitoringSystems m where type = :type and m.location = :location", OnmsMinion.class);
        query.setParameter("type", OnmsMonitoringSystem.TYPE_MINION);
        query.setParameter("location", locationName);

        return query.getResultList();
    }
}
