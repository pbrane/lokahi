/*******************************************************************************
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
 *******************************************************************************/

package org.opennms.horizon.minion.grpc.queue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;


public class RocksDbStore implements OffHeapSendQueueFactory.Store {

    private final Options rocksDBOptions;
    private final RocksDB rocksDB;

    public RocksDbStore() throws RocksDBException, IOException {
        this(Paths.get("./sink/queue").toAbsolutePath());
    }

    public RocksDbStore(final Path path) throws IOException, RocksDBException {
        this.rocksDBOptions = new Options()
            .setCreateIfMissing(true)
            .setCompressionType(CompressionType.SNAPPY_COMPRESSION)
            .setEnableBlobFiles(true)
            .setEnableBlobGarbageCollection(true)
            .setMinBlobSize(100_000L)
            .setBlobCompressionType(CompressionType.SNAPPY_COMPRESSION)
            .setCreateMissingColumnFamilies(true)
            .setTargetFileSizeBase(64L * 1024L * 1024L)
            .setMaxBytesForLevelBase(64L * 1024L * 1024L * 10L)
            .setMaxBackgroundJobs(Math.max(Runtime.getRuntime().availableProcessors(), 3));

        Files.createDirectories(path);

        this.rocksDB = RocksDB.open(this.rocksDBOptions, path.toString());
    }

    @Override
    public void close() {
        this.rocksDB.close();
        this.rocksDBOptions.close();
    }

    @Override
    public byte[] get(final byte[] key) throws IOException {
        try {
            return this.rocksDB.get(key);
        } catch (RocksDBException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void put(final byte[] key, final byte[] message) throws IOException {
        try {
            this.rocksDB.put(key, message);
        } catch (RocksDBException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void iterate(final Prefix prefix, final Consumer<byte[]> f) {
        try (final var it = this.rocksDB.newIterator()) {
            it.seek(prefix.getBytes());

            while (it.isValid() && prefix.of(it.key())) {
                f.accept(it.key());
                it.next();
            }
        }
    }
}
