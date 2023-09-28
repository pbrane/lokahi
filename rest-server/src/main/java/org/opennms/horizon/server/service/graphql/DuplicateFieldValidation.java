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

import graphql.GraphQLError;
import graphql.analysis.QueryReducer;
import graphql.analysis.QueryTraverser;
import graphql.analysis.QueryVisitorFieldEnvironment;
import graphql.execution.ExecutionContext;
import graphql.execution.instrumentation.fieldvalidation.FieldValidation;
import graphql.execution.instrumentation.fieldvalidation.FieldValidationEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DuplicateFieldValidation implements FieldValidation {

    private final int maxFieldOccurrence;

    @Override
    public List<GraphQLError> validateFields(FieldValidationEnvironment validationEnvironment) {
        QueryTraverser queryTraverser = newQueryTraverser(validationEnvironment.getExecutionContext());
        var occurrences = queryTraverser.reducePreOrder(new Reducer(), new HashMap<>());

        return occurrences
            .entrySet().stream()
            .filter(entry -> entry.getValue() > maxFieldOccurrence)
            .map(Map.Entry::getKey)
            .map(field -> validationEnvironment.mkError(
                "Validation error: Field '" + field.getName() + "' is repeated too many times"
            ))
            .collect(Collectors.toList());
    }

    QueryTraverser newQueryTraverser(ExecutionContext executionContext) {
        return QueryTraverser.newQueryTraverser()
            .schema(executionContext.getGraphQLSchema())
            .document(executionContext.getDocument())
            .operationName(executionContext.getExecutionInput().getOperationName())
            .coercedVariables(executionContext.getCoercedVariables())
            .build();
    }

    private static class Reducer implements QueryReducer<Map<GraphQLFieldDefinition, Integer>> {

        // TODO: No GraphQLSchemaElement implements `equals/hashCode` because
        //  we need object identity to treat a GraphQLSchema as an abstract graph.
        @Override
        public Map<GraphQLFieldDefinition, Integer> reduceField(
            QueryVisitorFieldEnvironment fieldEnvironment,
            Map<GraphQLFieldDefinition, Integer> acc
        ) {
            var key = fieldEnvironment.getFieldDefinition();
            int count = acc.getOrDefault(key, 0) + 1;
            acc.put(key, count);

            return acc;
        }
    }
}
