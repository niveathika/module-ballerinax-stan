/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nats.streaming;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import org.ballerinalang.nats.connection.DefaultConnectionListener;
import org.ballerinalang.nats.connection.DefaultErrorListener;

import java.io.IOException;
import java.time.Duration;

/**
 * Wraps {@link StreamingConnectionFactory}.
 */
public class BallerinaNatsStreamingConnectionFactory {
    private final BMap<BString, Object> streamingConfig;
    private final String url;
    private final String clusterId;
    private final String clientId;

    private static final BString ACK_TIMEOUT = StringUtils.fromString("ackTimeoutInSeconds");
    private static final BString CONNECTION_TIMEOUT = StringUtils.fromString("connectionTimeoutInSeconds");
    private static final BString MAX_PUB_ACKS_IN_FLIGHT = StringUtils.fromString("maxPubAcksInFlight");
    private static final BString DISCOVERY_PREFIX = StringUtils.fromString("discoverPrefix");

    public BallerinaNatsStreamingConnectionFactory(String url, String clusterId, String clientId,
                                                   BMap<BString, Object> streamingConfig) {
        this.streamingConfig = streamingConfig;
        this.url = url;
        this.clusterId = clusterId;
        this.clientId = clientId;
    }

    public StreamingConnection createConnection() throws IOException, InterruptedException {
        Options.Builder opts = new Options.Builder();
        opts.natsUrl(url);
        opts.clientId(clientId);
        opts.clusterId(clusterId);

        if (streamingConfig != null && TypeUtils.getType(streamingConfig).getTag() == TypeTags.RECORD_TYPE_TAG) {
            opts.connectionListener(new DefaultConnectionListener());
            opts.errorListener(new DefaultErrorListener());
            opts.discoverPrefix(streamingConfig.getStringValue(DISCOVERY_PREFIX).getValue());
            opts.connectWait(Duration.ofSeconds(streamingConfig.getIntValue(CONNECTION_TIMEOUT)));
            opts.pubAckWait(Duration.ofSeconds(streamingConfig.getIntValue(ACK_TIMEOUT)));
            opts.maxPubAcksInFlight(streamingConfig.getIntValue(MAX_PUB_ACKS_IN_FLIGHT).intValue());
        }
        StreamingConnectionFactory streamingConnectionFactory = new StreamingConnectionFactory(opts.build());
        return streamingConnectionFactory.createConnection();
    }
}