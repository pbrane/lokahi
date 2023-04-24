/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
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
 *******************************************************************************/

package org.opennms.horizon.minion.taskset.worker.ignite.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.opennms.horizon.minion.taskset.worker.ignite.WorkerIgniteConfiguration;

/**
 * Combined class loader which delegates lookup to dynamic list of class loaders which can change over time.
 */
public class CompoundClassLoader extends ClassLoader {

    private final WorkerIgniteConfiguration workerIgniteConfiguration;
    private final List<ClassLoader> loaders;

    public CompoundClassLoader(WorkerIgniteConfiguration workerIgniteConfiguration, List<ClassLoader> loaders) {
        this.workerIgniteConfiguration = workerIgniteConfiguration;
        this.loaders = loaders;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader loader : loaders) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                // skip error
            }
        }
        return super.loadClass(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        final List<Enumeration<URL>> enums = loaders.stream()
                .map(cl -> {
                    try {
                        return cl.getResources(name);
                    } catch (IOException e) {
                    }
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new CompoundEnumeration<>(enums);
    }

    final class CompoundEnumeration<E> implements Enumeration<E> {
        private final List<Enumeration<E>> enums;
        private int index;

        public CompoundEnumeration(List<Enumeration<E>> enums) {
            this.enums = enums;
        }

        private boolean next() {
            while (index < enums.size()) {
                if (enums.get(index) != null && enums.get(index).hasMoreElements()) {
                    return true;
                }
                index++;
            }
            return false;
        }

        public boolean hasMoreElements() {
            return next();
        }

        public E nextElement() {
            if (!next()) {
                throw new NoSuchElementException();
            }
            return enums.get(index).nextElement();
        }
    }
}
