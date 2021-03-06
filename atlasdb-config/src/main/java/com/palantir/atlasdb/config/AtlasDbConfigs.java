/**
 * Copyright 2015 Palantir Technologies
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
package com.palantir.atlasdb.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.palantir.remoting.ssl.SslConfiguration;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;

public final class AtlasDbConfigs {

    public static final String ATLASDB_CONFIG_OBJECT_PATH = "/atlasdb";

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory()
            .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));

    static {
        OBJECT_MAPPER.setSubtypeResolver(new DiscoverableSubtypeResolver());
        OBJECT_MAPPER.registerModule(new GuavaModule());
        OBJECT_MAPPER.registerModule(new Jdk7Module());
    }

    private AtlasDbConfigs() {
        // uninstantiable
    }

    public static AtlasDbConfig load(File configFile) throws IOException {
        return load(configFile, ATLASDB_CONFIG_OBJECT_PATH);
    }

    public static AtlasDbConfig load(InputStream configStream) throws IOException {
        return loadFromStream(configStream, ATLASDB_CONFIG_OBJECT_PATH);
    }

    public static AtlasDbConfig load(File configFile, @Nullable String configRoot) throws IOException {
        JsonNode node = OBJECT_MAPPER.readTree(configFile);
        return getConfig(node, configRoot);
    }

    public static AtlasDbConfig loadFromString(String fileContents, @Nullable String configRoot) throws IOException {
        JsonNode node = OBJECT_MAPPER.readTree(fileContents);
        return getConfig(node, configRoot);
    }

    public static AtlasDbConfig loadFromStream(InputStream configStream, @Nullable String configRoot)
            throws IOException {
        JsonNode node = OBJECT_MAPPER.readTree(configStream);
        return getConfig(node, configRoot);
    }

    private static AtlasDbConfig getConfig(JsonNode node, @Nullable String configRoot) throws IOException {
        JsonNode configNode = findRoot(node, configRoot);

        if (configNode == null) {
            throw new IllegalArgumentException("Could not find " + configRoot + " in input");
        }

        return OBJECT_MAPPER.treeToValue(configNode, AtlasDbConfig.class);
    }

    private static JsonNode findRoot(JsonNode node, @Nullable String configRoot) {
        if (Strings.isNullOrEmpty(configRoot)) {
            return node;
        }

        JsonNode root = node.at(JsonPointer.valueOf(configRoot));
        if (root.isMissingNode()) {
            return null;
        }
        return root;
    }

    public static AtlasDbConfig addFallbackSslConfigurationToAtlasDbConfig(
            AtlasDbConfig config,
            Optional<SslConfiguration> sslConfiguration) {
        return ImmutableAtlasDbConfig.builder()
                .from(config)
                .leader(addFallbackSslConfigurationToLeader(config.leader(), sslConfiguration))
                .lock(addFallbackSslConfigurationToServerList(config.lock(), sslConfiguration))
                .timestamp(addFallbackSslConfigurationToServerList(config.timestamp(), sslConfiguration))
                .build();
    }

    private static Optional<LeaderConfig> addFallbackSslConfigurationToLeader(
            Optional<LeaderConfig> config,
            Optional<SslConfiguration> sslConfiguration) {
        return config.transform(leader -> ImmutableLeaderConfig.builder()
                .from(leader)
                .sslConfiguration(leader.sslConfiguration().or(sslConfiguration))
                .build());
    }

    private static Optional<ServerListConfig> addFallbackSslConfigurationToServerList(
            Optional<ServerListConfig> config,
            Optional<SslConfiguration> sslConfiguration) {
        return config.transform(serverList -> ImmutableServerListConfig.builder()
                .from(serverList)
                .sslConfiguration(serverList.sslConfiguration().or(sslConfiguration))
                .build());
    }
}
