/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static org.openhab.binding.yandexstation.internal.yandexapi.QuasarApi.FILE_SCENARIOS;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.QuasarApi;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIScenarioResponse;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexScenariosHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(YandexScenariosHandler.class);

    private @Nullable ScheduledFuture<?> refreshPollingJob;
    public static final int reconnectInterval = 15;
    @Nullable
    YandexStationBridge yandexStationBridge;
    private @Nullable Future<?> initJob;
    private QuasarApi quasar;
    private WebSocketClient webSocketClient = new WebSocketClient();
    private YandexStationWebsocket yandexStationWebsocket = new YandexStationWebsocket();
    private ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
    Map<Integer, YandexStationScenarios> scenarioList = new HashMap<>();
    APIScenarioResponse scenarioResponse = new APIScenarioResponse();
    Map<String, String> device = new HashMap<>();
    private String url = "";
    char[] base_chars = ",.:".toCharArray();
    char[] digits = "01234567890".toCharArray();
    boolean dispose;

    /**
     * Instantiates a new Yandex station handler.
     *
     * @param thing the thing
     * @param apiFactory the api factory
     * @throws ApiException the api exception
     */
    public YandexScenariosHandler(Thing thing, YandexApiFactory apiFactory) throws ApiException {
        super(thing);
        this.quasar = (QuasarApi) apiFactory.getApiOnline(Objects.requireNonNull(thing.getBridgeUID()).getId());
    }

    private void saveScenariosToFile() {
        File f = quasar.getFile(FILE_SCENARIOS);
        if (f.exists()) {
            f.delete();
        }
        Arrays.stream(scenarioResponse.scenarios).forEach(scenario -> {
            try {
                Files.writeString(f.toPath(), scenario.id + ": " + scenario.name, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void updateScenarios() throws ApiException {
        List<Channel> channels = thing.getChannels();
        var context = new Object() {
            int x = 0;
        };

        for (Channel channel : channels) {
            boolean isNew = true;
            for (APIScenarioResponse.Scenarios scenario : scenarioResponse.scenarios) {
                if (scenario.name.startsWith(SEPARATOR_CHARS)) {
                    if (Objects.equals(channel.getLabel(), scenario.name.substring(4))) {
                        YandexStationScenarios yaScenario = new YandexStationScenarios();
                        yaScenario.addScenario(scenario, channel);
                        scenarioList.put(context.x, yaScenario);
                        String json = yaScenario.updateScenario(encode(context.x));
                        if (quasar.updateScenario(scenario.id, json)) {
                            logger.debug("scenario \"{}\" updated successfully", channel.getLabel());
                        } else {
                            logger.error("fail to update scenario \"{}\"", channel.getLabel());
                        }
                        context.x++;
                        isNew = false;
                    }
                }
            }
            if (isNew) {
                logger.debug("Channel \"{}\" is new. Creating...", channel.getLabel());
                YandexStationScenarios scenario = new YandexStationScenarios();
                String json = scenario.createScenario(channel, encode(context.x));
                try {
                    if (quasar.createScenario(json)) {
                        logger.debug("scenario \"{}\" created successfully", channel.getLabel());
                    } else {
                        logger.error("fail to create scenario \"{}\"", channel.getLabel());
                    }
                } catch (ApiException ignored) {
                }
                scenarioList.put(context.x, scenario);
                context.x++;
            }
        }
    }

    private void deleteScenarios() throws ApiException {
        for (APIScenarioResponse.Scenarios scenario : scenarioResponse.scenarios) {
            var ref = new Object() {
                boolean present = false;
            };
            scenarioList.forEach((k, v) -> {
                if (v.getScenarios() != null) {
                    if (v.getScenarios().id.equals(scenario.id)) {
                        ref.present = true;
                    }
                }
            });
            if (!ref.present) {
                if (scenario.name.startsWith(SEPARATOR_CHARS)) {
                    if (quasar.deleteScenario(scenario.id)) {
                        logger.debug("scenario with id \"{}\" deleted successfully", scenario.id);
                    } else {
                        logger.error("fail to delete scenario with id \"{}\"", scenario.id);
                    }
                }
            }
        }
    }

    private void initScenarios() throws ApiException {
        url = quasar.getWssUrl();
        scenarioResponse = quasar.getScenarios();
        saveScenariosToFile();

        device = quasar.getDevices();
        updateScenarios();
        deleteScenarios();
    }

    @Override
    public void initialize() {
        scenarioList = new HashMap<>();
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
                initScenarios();
            } catch (ApiException e) {
                logger.debug("Error {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }

            initJob = connect(0);
        }
    }

    private void startRefreshPollingJob() {
        if (refreshPollingJob == null || refreshPollingJob.isCancelled()) {
            refreshPollingJob = scheduler.scheduleWithFixedDelay(() -> ping(), 0, 1, TimeUnit.MINUTES);
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

    private Future<?> connect(int wait) {
        logger.warn("Yandex Scenario try connect websocket in {} sec", wait);
        return scheduler.schedule(() -> {
            boolean thingReachable = connectStation(url);
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
                startRefreshPollingJob();
            }
        }, wait, TimeUnit.SECONDS);
    }

    private boolean connectStation(String url) {
        @Nullable
        URI websocketAddress;
        try {
            websocketAddress = new URI(url);
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Initialize web socket failed: " + e.getMessage());
            logger.error("Initialize web socket failed: {}", e.getMessage());
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
                if (!dispose) {
                    reconnectWebsocket();
                } else {
                    dispose = false;
                }
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
                            updateChannel(
                                    message.get("updated_devices").getAsJsonArray().get(0).getAsJsonObject()
                                            .get("capabilities").getAsJsonArray().get(0).getAsJsonObject().get("state")
                                            .getAsJsonObject().get("value").getAsString(),
                                    message.get("updated_devices").getAsJsonArray().get(0).getAsJsonObject().get("id")
                                            .getAsString());
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
            logger.info("YandexScenarios connect to {}", websocketAddress);
            Future<?> session = webSocketClient.connect(yandexStationWebsocket, websocketAddress, clientUpgradeRequest);
            return session.isDone();
        } catch (Exception e) {
            // logger.error("Connection error {}", e.getMessage());
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement s : e.getStackTrace()) {
                sb.append(s.toString()).append("\n");
            }
            logger.error("Connection error: {}. Stacktrace: \n{}", e.getMessage(), sb);
            reconnectWebsocket();
            return false;
        }
    }

    private void updateChannel(String value, String id) {
        String subst = value.split(SEPARATOR_CHARS)[1];
        int yaScnId = decode(subst);
        if (scenarioList.containsKey(yaScnId)) {
            YandexStationScenarios scn = scenarioList.get(yaScnId);
            Map<String, String> device = this.device;
            String event = device.get(id);
            if (event != null) {
                triggerChannel(Objects.requireNonNull(scn.getChannel()).getUID(), event);
                updateState(scn.getChannel().getUID(), OnOffType.ON);
            } else {
                logger.warn("Device with id {} is not recognized", id);
                triggerChannel(Objects.requireNonNull(scn.getChannel()).getUID());
                updateState(scn.getChannel().getUID(), OnOffType.ON);
            }
        } else {
            logger.error("unknown scenario {} executed", yaScnId);
        }
    }

    private void reconnectWebsocket() {
        logger.debug("Yandex Scenario Handler try to reconnect websocket");
        try {
            url = quasar.getWssUrl();
        } catch (ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }

        cancelInitJob();
        cancelPollingJob();
        initJob = connect(reconnectInterval);
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

    private void ping() {
        yandexStationWebsocket.sendMessage("{\"ping\"}");
    }

    private void cancelInitJob() {
        Future<?> job = initJob;
        if (job != null) {
            job.cancel(true);
            initJob = null;
        }
    }

    private void cancelPollingJob() {
        Future<?> job = refreshPollingJob;
        if (job != null) {
            job.cancel(true);
            refreshPollingJob = null;
        }
    }

    @Override
    public void dispose() {
        dispose = true;
        logger.debug("{} dispose", getThing().getLabel());
        try {
            webSocketClient.stop();
            cancelInitJob();
            cancelPollingJob();
        } catch (Exception ignored) {
        }
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }
}
