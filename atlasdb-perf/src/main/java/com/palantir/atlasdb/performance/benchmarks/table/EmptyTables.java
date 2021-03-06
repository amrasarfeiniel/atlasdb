/**
 * Copyright 2016 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.performance.benchmarks.table;

import java.util.Map;
import java.util.Random;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import com.google.common.collect.Sets;
import com.palantir.atlasdb.keyvalue.api.Cell;
import com.palantir.atlasdb.keyvalue.api.KeyValueService;
import com.palantir.atlasdb.keyvalue.api.TableReference;
import com.palantir.atlasdb.performance.backend.AtlasDbServicesConnector;
import com.palantir.atlasdb.performance.benchmarks.Benchmarks;
import com.palantir.atlasdb.services.AtlasDbServices;
import com.palantir.atlasdb.transaction.api.TransactionManager;

/**
 * State class for creating a single Atlas table with one wide row.
 */
@State(Scope.Benchmark)
public class EmptyTables {

    private Random random = new Random(Tables.RANDOM_SEED);

    private AtlasDbServicesConnector connector;
    private AtlasDbServices services;

    public TransactionManager getTransactionManager() {
        return services.getTransactionManager();
    }

    public KeyValueService getKvs() {
        return services.getKeyValueService();
    }

    public TableReference getFirstTableRef() {
        return TableReference.createFromFullyQualifiedName("performance.table1");
    }

    public TableReference getSecondTableRef() {
        return TableReference.createFromFullyQualifiedName("performance.table2");
    }

    @Setup(Level.Trial)
    public void setup(AtlasDbServicesConnector conn) {
        this.connector = conn;
        this.services = conn.connect();
        Benchmarks.createTable(services.getKeyValueService(),
                getFirstTableRef(),
                Tables.ROW_COMPONENT,
                Tables.COLUMN_NAME);
        Benchmarks.createTable(services.getKeyValueService(),
                getSecondTableRef(),
                Tables.ROW_COMPONENT,
                Tables.COLUMN_NAME);
        makeTableEmpty();
    }

    @TearDown(Level.Invocation)
    public void makeTableEmpty() {
        this.services.getKeyValueService().truncateTables(Sets.newHashSet(getFirstTableRef(), getSecondTableRef()));
    }

    @TearDown(Level.Trial)
    public void cleanup() throws Exception {
        this.services.getKeyValueService().dropTables(Sets.newHashSet(
                getFirstTableRef(), getSecondTableRef()));
        this.connector.close();
    }

    public Map<Cell, byte[]> generateBatchToInsert(int size) {
        return Tables.generateRandomBatch(random, size);
    }

}
