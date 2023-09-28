/*
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
 */

package org.opennms.horizon.server.service.graphql;

import graphql.language.Directive;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;


// TODO: Name
@Slf4j
public class FieldOccurrenceCountVisitor extends NodeVisitorStub {

    @Getter
    private final Map<String, Integer> occurrences;

    public FieldOccurrenceCountVisitor() {
        occurrences = new LinkedHashMap<>();
    }

    @Override
    public TraversalControl visitDirective(Directive node, TraverserContext<Node> context) {
        int count = occurrences.getOrDefault(node.getName(), 0) + 1;
        occurrences.put(node.getName(), count);
        return TraversalControl.CONTINUE;
    }
}
