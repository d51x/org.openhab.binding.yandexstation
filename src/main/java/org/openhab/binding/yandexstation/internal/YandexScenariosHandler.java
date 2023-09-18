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

import static org.openhab.binding.yandexstation.internal.YandexStationScenarios.SEPARATOR_CHARS;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiOnline;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIExtendedResponse;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIScenarioResponse;
import org.openhab.core.library.types.OnOffType;
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
    private final YandexApiOnline api;
    private WebSocketClient webSocketClient = new WebSocketClient();
    private YandexStationWebsocket yandexStationWebsocket = new YandexStationWebsocket();
    private ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
    Map<Integer, YandexStationScenarios> scnList = new HashMap<>();
    APIScenarioResponse scenario = new APIScenarioResponse();
    private String url = "";
    char[] base_chars = ",.:".toCharArray();
    char[] digits = "01234567890".toCharArray();

    public YandexScenariosHandler(Thing thing, YandexApiFactory apiFactory) throws ApiException {
        super(thing);
        this.api = (YandexApiOnline) apiFactory.getToken();
    }

    @Override
    public void initialize() {
        scnList = new HashMap<>();
        updateStatus(ThingStatus.UNKNOWN);
        yandexStationBridge = getBridgeHandler();
        if (yandexStationBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Check bridge");
        } else {
            while (yandexStationBridge.getThing().getStatus() != ThingStatus.ONLINE) {
                if (!yandexStationBridge.getThing().isEnabled()) {
                    break;
                }
            }
            try {
                url = api.getWssUrl();
                scenario = api.getScenarios();
                List<Channel> channels = thing.getChannels();
                var context = new Object() {
                    int x = 0;
                };
                channels.forEach(channel -> {
                    boolean isNew = true;
                    for (APIScenarioResponse.Scenarios scn : scenario.scenarios) {
                        if (scn.name.startsWith(SEPARATOR_CHARS)) {
                            if (Objects.equals(channel.getLabel(), scn.name.substring(4))) {
                                YandexStationScenarios yaScn = new YandexStationScenarios();
                                yaScn.addScenario(scn, channel, encode(context.x));
                                scnList.put(context.x, yaScn);
                                String json = yaScn.updateScenario(encode(context.x));
                                APIExtendedResponse response = api.sendPutJsonRequest(
                                        "https://iot.quasar.yandex.ru/m/user/scenarios/" + scn.id, json, "");
                                logger.debug("response script update: {}", response.response);
                                context.x++;
                                isNew = false;
                            }
                        }
                    }
                    if (isNew) {
                        logger.debug("Channel {} is new. Creating...", channel.getLabel());
                        YandexStationScenarios yaScn = new YandexStationScenarios();
                        String json = yaScn.createScenario(channel, encode(context.x));
                        try {
                            APIExtendedResponse response = api
                                    .sendPostJsonRequest("https://iot.quasar.yandex.ru/m/user/scenarios", json, "");
                            logger.debug("response script creation: {}", response.response);
                        } catch (ApiException ignored) {
                        }
                        scnList.put(context.x, yaScn);
                        context.x++;
                        isNew = false;
                    }
                });
                for (APIScenarioResponse.Scenarios scn : scenario.scenarios) {
                    var ref = new Object() {
                        boolean present = false;
                    };
                    scnList.forEach((k, v) -> {
                        if (v.getScn() != null) {
                            if (v.getScn().id.equals(scn.id)) {
                                ref.present = true;
                            }
                        }
                    });
                    if (!ref.present) {
                        if (scn.name.startsWith(SEPARATOR_CHARS)) {
                            APIExtendedResponse response = api
                                    .sendDeleteJsonRequest("https://iot.quasar.yandex.ru/m/user/scenarios/" + scn.id);
                            logger.debug("response script delete: {}", response.response);
                        }
                    }
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
        }, 1, TimeUnit.SECONDS);
    }

    private boolean connectStation(String url) {
        @Nullable
        URI websocketAddress;
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
            public void onClose(int statusCode, String reason) {
                logger.debug("Websocket connection closed");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Connection closed: " + statusCode + " - " + reason);
            }

            @Override
            public void onMessage(String data) {
                JsonObject json = JsonParser.parseString(data).getAsJsonObject();
                if (json.get("operation").getAsString().equals("update_states")) {
                    JsonObject message = JsonParser.parseString(json.get("message").getAsString()).getAsJsonObject();
                    if (message.get("updated_devices").getAsJsonArray().get(0).getAsJsonObject().has("capabilities")) {
                        if (message.get("updated_devices").getAsJsonArray().get(0).getAsJsonObject().get("capabilities")
                                .getAsJsonArray().get(0).getAsJsonObject().get("type").getAsString()
                                .equals("devices.capabilities.quasar.server_action")) {
                            updateChannel(message.get("updated_devices").getAsJsonArray().get(0).getAsJsonObject()
                                    .get("capabilities").getAsJsonArray().get(0).getAsJsonObject().get("state")
                                    .getAsJsonObject().get("value").getAsString());
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable cause) {
                logger.error("Websocket error: {}", cause.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, cause.getMessage());
                reconnectWebsocket();
            }
        });

        try {
            webSocketClient.start();
            Future<?> session = webSocketClient.connect(yandexStationWebsocket, websocketAddress, clientUpgradeRequest);
            return session.isDone();
        } catch (Exception e) {
            logger.error("Connection error {}", e.getMessage());
            reconnectWebsocket();
            return false;
        }
    }

    private void updateChannel(String value) {
        String subst = value.split(SEPARATOR_CHARS)[1];
        int yaScnId = decode(subst);
        YandexStationScenarios scn = scnList.get(yaScnId);
        updateState(scn.getChannel().getUID(), OnOffType.ON);
    }

    private void reconnectWebsocket() {
        logger.debug("Try to reconnect");
        Future<?> job = initJob;
        if (job != null) {
            job.cancel(true);
            initJob = null;
        }
        initJob = connect();
    }

    public String encode(int number) {
        String character = "";
        int x = 0;
        char[] nmb = String.valueOf(number).toCharArray();
        for (char digit : nmb) {
            x = x * digits.length + String.valueOf(digits).indexOf(digit);
        }
        if (x == 0) {
            character = String.valueOf(base_chars[0]);
        } else {
            while (x > 0) {
                int dig = x % base_chars.length;
                character = base_chars[dig] + character;
                x = x / base_chars.length;
            }
        }
        return character;
    }

    public int decode(String encode) {
        String character = "";
        int x = 0;
        char[] nmb = encode.toCharArray();
        for (char digit : nmb) {
            x = x * base_chars.length + String.valueOf(base_chars).indexOf(digit);
        }
        if (x == 0) {
            character = String.valueOf(digits[0]);
        } else {
            while (x > 0) {
                int dig = x % digits.length;
                character = digits[dig] + character;
                x = x / digits.length;
            }
        }
        return Integer.parseInt(character);
    }

    @Override
    public void dispose() {
        logger.debug("dispose");
        try {
            webSocketClient.stop();
            Future<?> job = initJob;
            if (job != null) {
                job.cancel(true);
                initJob = null;
            }
        } catch (Exception ignored) {
        }
        super.dispose();
    }
}