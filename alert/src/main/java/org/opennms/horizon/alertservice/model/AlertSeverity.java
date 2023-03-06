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

package org.opennms.horizon.alertservice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AlertSeverity implements Serializable {
    // Keep this ordered by ID so we can use the internal enum compareTo
    UNDEFINED(0, "Undefined", "lightblue"),
    INDETERMINATE(1, "Indeterminate", "lightblue"),
    CLEARED(2, "Cleared", "white"),
    NORMAL(3, "Normal", "green"),
    WARNING(4, "Warning", "cyan"),
    MINOR(5, "Minor", "yellow"),
    MAJOR(6, "Major", "orange"),
    CRITICAL(7, "Critical", "red");
    
    private static final Map<Integer, AlertSeverity> idMap;
    
    private int id;
    private String label;
    private String color;

    static {
        idMap = new HashMap<Integer, AlertSeverity>(values().length);
        for (final AlertSeverity severity : values()) {
            idMap.put(severity.getId(), severity);
        }
    }

    private AlertSeverity(final int id, final String label, final String color) {
        this.id = id;
        this.label = label;
        this.color = color;
    }
    
    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return id;
    }
    
    /**
     * <p>getLabel</p>
     *
     * @return a {@link String} object.
     */
    public String getLabel() {
        return label;
    }

    /**
     * <p>getColor</p>
     *
     * @return a {@link String} object.
     */
    public String getColor() {
        return color;
    }

    /**
     * <p>isLessThan</p>
     *
     * @param other a {@link AlertSeverity} object.
     * @return a boolean.
     */
    public boolean isLessThan(final AlertSeverity other) {
        return compareTo(other) < 0;
    }

    /**
     * <p>isLessThanOrEqual</p>
     *
     * @param other a {@link AlertSeverity} object.
     * @return a boolean.
     */
    public boolean isLessThanOrEqual(final AlertSeverity other) {
        return compareTo(other) <= 0;
    }

    /**
     * <p>isGreaterThan</p>
     *
     * @param other a {@link AlertSeverity} object.
     * @return a boolean.
     */
    public boolean isGreaterThan(final AlertSeverity other) {
        return compareTo(other) > 0;
    }
    
    /**
     * <p>isGreaterThanOrEqual</p>
     *
     * @param other a {@link AlertSeverity} object.
     * @return a boolean.
     */
    public boolean isGreaterThanOrEqual(final AlertSeverity other) {
        return compareTo(other) >= 0;
    }
    
    /**
     * <p>get</p>
     *
     * @param id a int.
     * @return a {@link AlertSeverity} object.
     */
    public static AlertSeverity get(final int id) {
        if (idMap.containsKey(id)) {
            return idMap.get(id);
        } else {
            throw new IllegalArgumentException("Cannot create AlertSeverity from unknown ID " + id);
        }
    }

    /**
     * <p>get</p>
     *
     * @param label a {@link String} object.
     * @return a {@link AlertSeverity} object.
     */
    public static AlertSeverity get(final String label) {
        for (final Integer key : idMap.keySet()) {
            if (idMap.get(key).getLabel().equalsIgnoreCase(label)) {
                return idMap.get(key);
            }
        }
        return AlertSeverity.INDETERMINATE;
    }

    /**
     * <p>escalate</p>
     *
     * @param sev a {@link AlertSeverity} object.
     * @return a {@link AlertSeverity} object.
     */
    public static AlertSeverity escalate(final AlertSeverity sev) {
        if (sev.isLessThan(AlertSeverity.CRITICAL)) {
            return AlertSeverity.get(sev.getId()+1);
        } else {
            return AlertSeverity.get(sev.getId());
        }
    }

    public static List<String> names() {
        final List<String> names = new ArrayList<>();
        for (final AlertSeverity value : values()) {
            names.add(value.toString());
        }
        return names;
    }
}
