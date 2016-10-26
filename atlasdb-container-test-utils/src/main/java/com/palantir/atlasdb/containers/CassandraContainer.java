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
package com.palantir.atlasdb.containers;

import java.net.InetSocketAddress;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.palantir.atlasdb.cassandra.CassandraKeyValueServiceConfig;
import com.palantir.atlasdb.cassandra.CassandraKeyValueServiceConfigManager;
import com.palantir.atlasdb.cassandra.ImmutableCassandraCredentialsConfig;
import com.palantir.atlasdb.cassandra.ImmutableCassandraKeyValueServiceConfig;
import com.palantir.atlasdb.config.ImmutableLeaderConfig;
import com.palantir.atlasdb.config.LeaderConfig;
import com.palantir.atlasdb.keyvalue.cassandra.CassandraKeyValueService;
import com.palantir.docker.compose.connection.waiting.SuccessOrFailure;

public class CassandraContainer extends Container {
    public static final CassandraKeyValueServiceConfig KVS_CONFIG = ImmutableCassandraKeyValueServiceConfig.builder()
            .addServers(new InetSocketAddress("cassandra", 9160))
            .poolSize(20)
            .keyspace("atlasdb")
            .credentials(ImmutableCassandraCredentialsConfig.builder()
                    .username("cassandra")
                    .password("cassandra")
                    .build())
            .replicationFactor(1)
            .mutationBatchCount(10000)
            .mutationBatchSizeBytes(10000000)
            .fetchBatchCount(1000)
            .safetyDisabled(false)
            .autoRefreshNodes(false)
            .build();

    public static final Optional<LeaderConfig> LEADER_CONFIG = Optional.of(ImmutableLeaderConfig
            .builder()
            .quorumSize(1)
            .localServer("localhost")
            .leaders(ImmutableSet.of("localhost"))
            .build());

    @Override
    public String getDockerComposeFile() {
        return "/docker-compose-cassandra.yml";
    }

    @Override
    public SuccessOrFailure isReady() {
        return SuccessOrFailure.onResultOf(() -> {
            CassandraKeyValueService.create(
                    CassandraKeyValueServiceConfigManager.createSimpleManager(KVS_CONFIG),
                    LEADER_CONFIG);
            return true;
        });
    }

    public boolean equals(Object obj) {
        return obj instanceof CassandraContainer;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
