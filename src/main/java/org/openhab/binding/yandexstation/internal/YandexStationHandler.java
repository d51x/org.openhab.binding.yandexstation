/*
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information.
 *
 * This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yandexstation.internal;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yandexstation.internal.actions.things.YandexStationThingActions;
import org.openhab.binding.yandexstation.internal.commands.*;
import org.openhab.binding.yandexstation.internal.response.YandexStationPlayerState;
import org.openhab.binding.yandexstation.internal.response.YandexStationResponse;
import org.openhab.binding.yandexstation.internal.response.YandexStationState;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiDeviceResponse;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiImpl;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.*;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.yandexstation.internal.YandexStationChannels.*;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.*;

/**
 * The {@link YandexStationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexStationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(YandexStationHandler.class);

    private @Nullable YandexStationConfiguration config;
    private @Nullable Future<?> initJob;
    private WebSocketClient webSocketClient = new WebSocketClient();
    private YandexStationWebsocket yandexStationWebsocket = new YandexStationWebsocket();
    private ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
    private @Nullable URI websocketAddress;
    private @Nullable YandexApiImpl api;

    private YandexStationState stationState = new YandexStationState();

    /**
     * Instantiates a new Yandex station handler.
     *
     * @param thing the thing
     * @param apiFactory the api factory
     * @throws ApiException the api exception
     */
    public YandexStationHandler(Thing thing, YandexApiFactory apiFactory) throws ApiException {
        super(thing);
        this.api = (YandexApiImpl) apiFactory.getApi();
    }

    private Double prevVolume = 0.0;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_COMMAND_VOICE.getName().equals(channelUID.getId())) {
            if (command instanceof StringType) {
                sendVoiceCommand(command.toString());
            }

        } else if (CHANNEL_COMMAND_TTS.getName().equals(channelUID.getId())) {
            if (command instanceof StringType) {
                sendTtsCommand(command.toString());
            }
        } else if (CHANNEL_VOLUME.getName().equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                sendSetVolumeCommand(((DecimalType) command).doubleValue());
            }
        } else if (CHANNEL_STATE_TRACK_POSITION.getName().equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                sendTrackPositionCommand(((DecimalType) command).intValue());
            }
        } else if (CHANNEL_PLAYER_CONTROL.getName().equals(channelUID.getId())) {
            if (command instanceof PlayPauseType) {
                PlayPauseType cmd = (PlayPauseType) command;
                switch (cmd) {
                    case PLAY:
                        sendPlayCommand();
                        break;
                    case PAUSE:
                        sendStopCommand();
                        break;
                }
            }
            if (command instanceof NextPreviousType) {
                NextPreviousType cmd = (NextPreviousType) command;
                switch (cmd) {
                    case NEXT:
                        sendPlayNextCommand();
                        break;
                    case PREVIOUS:
                        sendPlayPrevCommand();
                        break;
                }
            }
            if (command instanceof RewindFastforwardType) {
                RewindFastforwardType cmd = (RewindFastforwardType) command;
                switch (cmd) {
                    case FASTFORWARD:
                        fastForward();
                        break;
                    case REWIND:
                        fastRewind();
                        break;
                }
            }
            if (command instanceof StringType) {
                if (command.toString().equals("PLAY")) {
                    sendPlayCommand();
                } else if (command.toString().equals("PAUSE")) {
                    sendStopCommand();
                } else if (command.toString().equals("NEXT")) {
                    sendPlayNextCommand();
                } else if (command.toString().equals("PREVIOUS")) {
                    sendPlayPrevCommand();
                } else if (command.toString().equals("FASTFORWARD")) {
                    fastForward();
                } else if (command.toString().equals("REWIND")) {
                    fastRewind();
                }
            }
        } else if (CHANNEL_VOLUME_CONTROL.getName().equals(channelUID.getId())) {
            if (command instanceof StringType) {
                if (command.toString().equals("MUTE")) {
                    volumeMute();
                } else if (command.toString().equals("UNMUTE")) {
                    volumeMute();
                } else if (command.toString().equals("VOLUME_UP")) {
                    volumeUp();
                } else if (command.toString().equals("VOLUME_DOWN")) {
                    volumeDown();
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(YandexStationConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        initJob = connect(0);
    }

    @Override
    public void dispose() {
        super.dispose();

        try {
            webSocketClient.stop();
            Future<?> job = initJob;
            if (job != null) {
                job.cancel(true);
                initJob = null;
            }
        } catch (Exception e) {
            logger.error("Could not stop webSocketClient,  message {}", e.getMessage());
        }
    }

    @Override
    protected void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }

    private Future<?> connect(int wait) {
        logger.warn("Try connect after: {} sec", wait);
        return scheduler.schedule(() -> {
            if (config == null) {
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR);
            } else {
                updateStatus(ThingStatus.OFFLINE);
                if (config.device_token.isEmpty()) {
                    logger.warn("Device token is empty");
                    receiveDeviceToken();
                }
                boolean thingReachable = connectStation(config);
                if (thingReachable) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        }, wait, TimeUnit.SECONDS);
    }

    private boolean connectStation(@Nullable YandexStationConfiguration config) {
        try {
            websocketAddress = new URI("wss://" + config.hostname + ":" + config.port);
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

                    ping(config.device_token);
                    requestSoftwareVersion(config.device_token);
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

                if (statusCode == 4000) {
                    receiveDeviceToken();
                }
                reconnectWebsocket();
            }

            @Override
            public void onMessage(String data) {
                logger.debug("Data received: {}", data);
                Gson gson = new Gson();
                YandexStationResponse response = gson.fromJson(data, YandexStationResponse.class);
                if (response != null) {
                    processReceivedData(response);
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
            return false;
        }
    }

    private void reconnectWebsocket() {
        logger.debug("Try to reconnect");
        Future<?> job = initJob;
        if (job != null) {
            job.cancel(true);
            initJob = null;
        }
        initJob = connect(config.reconnectInterval);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(YandexStationThingActions.class); // , YandexStationAction.class);
    }

    public void sendVoiceCommand(String text) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SENT_TEXT, text);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    public void sendTtsCommand(String text) {
        FormUpdate formUpdate = new FormUpdate();
        FormUpdateSlot slot = new FormUpdateSlot(text);
        formUpdate.addSlot(slot);
        ServerActionPayload payload = new ServerActionPayload(formUpdate, Boolean.TRUE);
        ServerActionEvent event = new ServerActionEvent("update_form", payload);
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SERVER_ACTION, event);

        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    public void sendStopListening() {
        ServerActionEvent event = new ServerActionEvent("on_suggest", null);
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SERVER_ACTION, event);

        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }


    private void sendSetVolumeCommand(Double volume) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SET_VOLUME, volume);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendTrackPositionCommand(Integer position) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_REWIND, position);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void volumeMute() {
        if (prevVolume > 0.0) {
            stationState.volume = prevVolume;
        } else {
            prevVolume = stationState.volume;
            stationState.volume = 0.0;
        }
        sendSetVolumeCommand(stationState.volume);
    }

    private void volumeUp() {
        if (stationState.volume < 1) {
            stationState.volume += 0.1;
            sendSetVolumeCommand(stationState.volume);
        }
    }

    private void volumeDown() {
        if (stationState.volume > 0) {
            stationState.volume -= 0.1;
            sendSetVolumeCommand(stationState.volume);
        }
    }

    private void fastForward() {
        Double position = stationState.playerState.getProgress() + 15.0;
        if (stationState.playerState.getDuration() > position) {
            sendTrackPositionCommand(position.intValue());
        }
    }

    private void fastRewind() {
        double position = stationState.playerState.getProgress() - 15.0;
        if (position > 0) {
            sendTrackPositionCommand((int) position);
        }
    }

    private void sendPlayNextCommand() {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_NEXT);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendPlayPrevCommand() {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PREV);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendPlayCommand() {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PLAY);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendStopCommand() {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_STOP);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void ping(String device_token) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PING);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void requestSoftwareVersion(String device_token) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SW_VERSION);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void processReceivedData(YandexStationResponse response) {
        if (response.getSoftwareVersion() != null) {
            updateState(CHANNEL_STATE_SOFTWARE.getName(), new StringType(response.getSoftwareVersion()));
            updateProperty("Software Version:", response.getSoftwareVersion());
        }
        if (response.getState() != null) {
            stationState = response.getState();
            if (stationState.aliceState != null) {
                updateState(CHANNEL_STATE_ALICE.getName(), new StringType(stationState.aliceState.toString()));
            }
            if (stationState.playing != null) {
                updateState(CHANNEL_STATE_PLAYING.getName(), new StringType(stationState.playing ? "PLAY" : "PAUSE"));
            }
            if (stationState.volume != null) {
                updateState(CHANNEL_VOLUME.getName(), new DecimalType(stationState.volume));
            }
            if (stationState.playing != null) {
                updateState(CHANNEL_STATE_PLAYING.getName(), OnOffType.from(stationState.playing));
            }
            processPlayerState(stationState.playerState);
        }
    }

    private void processPlayerState(YandexStationPlayerState playerState) {
        if (playerState.getDuration() != null) {
            updateState(CHANNEL_STATE_TRACK_DURATION.getName(), new DecimalType(playerState.getDuration()));
        }
        if (playerState.getHasProgressBar() != null && playerState.getHasProgressBar()
                && playerState.getProgress() != null) {
            updateState(CHANNEL_STATE_TRACK_POSITION.getName(), new DecimalType(playerState.getProgress()));
        }
        if (playerState.getPlaylistId() != null) {
            updateState(CHANNEL_STATE_TRACK_PLAYLIST_ID.getName(), new StringType(playerState.getPlaylistId()));
        }
        if (playerState.getId() != null) {
            updateState(CHANNEL_STATE_TRACK_ID.getName(), new DecimalType(playerState.getId()));
        }
        if (playerState.getPlaylistType() != null) {
            updateState(CHANNEL_STATE_PLAYLIST_TYPE.getName(), new StringType(playerState.getPlaylistType()));
        }
        if (playerState.getSubtitle() != null) {
            updateState(CHANNEL_STATE_TRACK_SUBTITLE.getName(), new StringType(playerState.getSubtitle()));
        }
        if (playerState.getTitle() != null) {
            updateState(CHANNEL_STATE_TRACK_TITLE.getName(), new StringType(playerState.getTitle()));
        }
        if (playerState.getType() != null) {
            updateState(CHANNEL_STATE_TRACK_TYPE.getName(), new StringType(playerState.getType()));
        }
        if (playerState.getExtra() != null) {
            if (playerState.getExtra().coverURI != null) {
                updateState(CHANNEL_STATE_TRACK_COVER_URI.getName(), new StringType(playerState.getExtra().coverURI));
            }
        }
        if (playerState.getEntityInfo() != null) {
            if (playerState.getEntityInfo().next != null && !playerState.getEntityInfo().next.isEmpty()
                    && playerState.getEntityInfo().next.containsKey("id")) {
                String sId = playerState.getEntityInfo().next.get("id");
                if (sId != null && !sId.isEmpty()) {
                    updateState(CHANNEL_STATE_TRACK_NEXT_ID.getName(), new DecimalType(DecimalType.valueOf(sId)));
                }
            }
            if (playerState.getEntityInfo().prev != null && !playerState.getEntityInfo().prev.isEmpty()
                    && playerState.getEntityInfo().prev.containsKey("id")) {
                String sId = playerState.getEntityInfo().prev.get("id");
                if (sId != null && !sId.isEmpty()) {
                    updateState(CHANNEL_STATE_TRACK_PREV_ID.getName(), new DecimalType(DecimalType.valueOf(sId)));
                }
            }
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
    }

    @Override
    protected void updateProperty(String name, @Nullable String value) {
        super.updateProperty(name, value);
    }

    private void receiveDeviceToken() {
        // get device token from https://quasar.yandex.net/glagol/token
        try {
            ApiDeviceResponse device = api.findDevice(config.device_id, config.yandex_token);
            String token = api.getDeviceToken(config.yandex_token, config.device_id, device.platform);

            config.platform = device.platform;
            config.device_token = token;

            config.hostname = device.networkInfo.ipAdresses.get(0);
            config.port = String.valueOf(device.networkInfo.port);

            Configuration configuration = thing.getConfiguration();
            configuration.put("device_token", token);
            configuration.put("platform", device.platform);
            configuration.put("hostname", config.hostname);
            configuration.put("port", config.port);
            configuration.put("server_certificate", device.glagol.security.serverCertificate);
            configuration.put("server_private_key", device.glagol.security.serverPrivateKey);

            setThingProperties(device);

            if (Boolean.FALSE.equals(YandexStationTypes.isLocalApi(device.platform))) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device doesn't support Local API");
                throw new RuntimeException(String.format("Device %s not supported local api", device.name));
            }
        } catch (ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void setThingProperties(ApiDeviceResponse device) {
        Map<String, String> properties = new HashMap<>();
        properties.put("Support Local API:",
                YandexStationTypes.isLocalApi(device.platform) ? "Supported" : "Not Supported");
        properties.put("Wifi SSID:", device.networkInfo.wifiSSID);
        properties.put("IP Address:", device.networkInfo.ipAdresses.get(0));
        properties.put("Platform:", device.platform);
        properties.put("Device Name:", YandexStationTypes.getNameByPlatform(device.platform));
        properties.put("Friendly Name:", device.name);
        updateProperties(properties);
    }

    public YandexStationState getStationState() {
        return stationState;
    }
}
