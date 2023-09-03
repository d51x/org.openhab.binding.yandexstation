/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yandexstation.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiGetTokens;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link YandexScenariosHandler} is describing implementaion of api interface.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexScenariosHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(YandexScenariosHandler.class);
    @Nullable
    YandexStationBridge yandexStationBridge;
    private @Nullable Future<?> initJob;
    private @Nullable YandexApiGetTokens api;
    private WebSocketClient webSocketClient = new WebSocketClient();
    private @Nullable URI websocketAddress;
    private YandexStationWebsocket yandexStationWebsocket = new YandexStationWebsocket();
    private ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
    private String url = "";

    public YandexScenariosHandler(Thing thing, YandexApiFactory apiFactory) throws ApiException {
        super(thing);
        this.api = (YandexApiGetTokens) apiFactory.getToken();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        yandexStationBridge = getBridgeHandler();
        if (yandexStationBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Check bridge");
        } else {
            while (yandexStationBridge.getThing().getStatus() != ThingStatus.ONLINE) {
            }
            // List<ApiDeviceResponse> devices = api.getDevices();
            try {
                if (api != null) {
                    url = api.getWssUrl();
                    logger.debug("Devices {}", url);
                }
            } catch (ApiException e) {
                logger.debug("Error {}", e.getMessage());
            }
            initJob = connect();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private synchronized @Nullable YandexStationBridge getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.error("Required bridge not defined for device.");
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized @Nullable YandexStationBridge getBridgeHandler(Bridge bridge) {
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof YandexStationBridge) {
            return (YandexStationBridge) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
            return null;
        }
    }

    private Future<?> connect() {
        logger.warn("Try connect after: {} sec", 0);
        return scheduler.schedule(() -> {
            boolean thingReachable = connectStation(url);
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            }
        }, 0, TimeUnit.SECONDS);
    }

    private boolean connectStation(String url) {
        try {
            websocketAddress = new URI(url);
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Initialize web socket failed: " + e.getMessage());
            return false;
        }

        webSocketClient.getSslContextFactory().setTrustAll(true);
        yandexStationWebsocket.addMessageHandler(new YandexStationWebsocketInterface() {
            @Override
            public void onConnect(boolean connected) {
                if (connected) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("websocket connected");
                } else {
                    logger.debug("websocket connection failed");
                    updateStatus(ThingStatus.OFFLINE);
                }
            }

            @Override
            public void onClose(int statusCode, String reason) throws Exception {
                logger.debug("Websocket connection closed");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Connection closed: " + statusCode + " - " + reason);
            }

            @Override
            public void onMessage(String data) {
                logger.debug("Data received: {}", data);
                // Gson gson = new Gson();
                JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            }

            @Override
            public void onError(Throwable cause) {
                logger.error("Websocket error: {}", cause.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, cause.getMessage());
                // reconnectWebsocket();
            }
        });

        try {
            webSocketClient.start();
            Future<?> session = webSocketClient.connect(yandexStationWebsocket, websocketAddress, clientUpgradeRequest);
            return session.isDone();
        } catch (Exception e) {
            logger.error("Connection error {}", e.getMessage());
            return false;
        }
    }
}
