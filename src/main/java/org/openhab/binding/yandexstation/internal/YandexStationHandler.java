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

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yandexstation.internal.commands.*;
import org.openhab.binding.yandexstation.internal.response.YandexStationPlayerState;
import org.openhab.binding.yandexstation.internal.response.YandexStationResponse;
import org.openhab.binding.yandexstation.internal.response.YandexStationState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.*;
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
    public String receivedMessage = "";

    private YandexStationState stationState = new YandexStationState();

    public YandexStationHandler(Thing thing) {
        super(thing);
    }

    private Double prevVolume = 0.0;
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_COMMAND_VOICE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                sendVoiceCommand(command.toString());
            }

        } else if (CHANNEL_COMMAND_TTS.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                sendTtsCommand(command.toString());
            }
        } else if (CHANNEL_VOLUME.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                sendSetVolumeCommand(((DecimalType) command).doubleValue());
            }
        } else if (CHANNEL_STATE_TRACK_POSITION.equals(channelUID.getId())) {
            if (command instanceof DecimalType) {
                sendTrackPositionCommand(((DecimalType) command).intValue());
            }
        } else if (CHANNEL_PLAYER_CONTROL.equals(channelUID.getId())) {
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
        } else if (CHANNEL_VOLUME_CONTROL.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                if (command.toString().equals("MUTE")) {
                    volumeMute();
                }  else if (command.toString().equals("UNMUTE")) {
                    volumeMute();
                }else if (command.toString().equals("VOLUME_UP")) {
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

        // Example for background initialization:
        initJob = connect(0);
    }

    private Future<?> connect(int wait) {
        logger.warn("Try connect after: {} sec", wait);
        return scheduler.schedule(() -> {
            if (config == null) {
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR);
            } else {
                boolean thingReachable = connectStation(config);
                logger.warn("thingReachable: {}", thingReachable);
                // when done do:
                if (thingReachable) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }, wait, TimeUnit.SECONDS);
    }

    private boolean connectStation(@Nullable YandexStationConfiguration config) {
        try {
            websocketAddress = new URI("wss://" + config.hostname + ":" + WSS_PORT);
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
                    logger.info("websocket connected");

                    ping(config.device_token);
                    requestSoftwareVersion(config.device_token);
                } else {
                    logger.error("websocket connection failed");
                    updateStatus(ThingStatus.OFFLINE);
                }
            }

            @Override
            public void onClose(int statusCode, String reason) {
                logger.info("Websocket connection closed");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Connection closed: " + statusCode + " - " + reason);
                //disposeWebsocketPollingJob();
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
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        cause.getMessage());
                //disposeWebsocketPollingJob();
                reconnectWebsocket();
            }
        });

        try {
            webSocketClient.start();
            Future<?> session = webSocketClient.connect(yandexStationWebsocket, websocketAddress, clientUpgradeRequest);
            if (session.isDone()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Connection error {}", e.getMessage());
            return false;
        }
        //return true;
    }

    @Override
    public void dispose() {
        super.dispose();

        //disposeWebsocketPollingJob();
        //disposeWebSocketReconnectionPollingJob();
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

    private void reconnectWebsocket() {
        logger.warn("Try to reconnect: {}", initJob);
        Future<?> job = initJob;
        if (job != null) {
            job.cancel(true);
            initJob = null;
        }
        initJob = connect(config.reconnectInterval);
    }

    private void sendVoiceCommand(String text) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SENT_TEXT, text);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendTtsCommand(String text) {
        FormUpdate formUpdate = new FormUpdate();
        FormUpdateSlot slot = new FormUpdateSlot(text);
        formUpdate.addSlot(slot);
        ServerActionPayload payload = new ServerActionPayload(formUpdate, Boolean.TRUE);
        ServerActionEvent event = new ServerActionEvent("update_form", payload);
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SERVER_ACTION, event);

        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendSetVolumeCommand(Double volume) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SET_VOLUME, volume);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendTrackPositionCommand(Integer position) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_REWIND, position);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
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
        Double position = stationState.playerState.getProgress() + 5.0;
        if (stationState.playerState.getDuration() > position) {
            sendTrackPositionCommand(position.intValue());
        }
    }

    private void fastRewind() {
        Double position = stationState.playerState.getProgress() - 5.0;
        if (position > 0) {
            sendTrackPositionCommand(position.intValue());
        }
    }

    private void sendPlayNextCommand() {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_NEXT);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendPlayPrevCommand() {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PREV);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendPlayCommand() {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PLAY);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendStopCommand() {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_STOP);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void ping(String device_token) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PING);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void requestSoftwareVersion(String device_token) {
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SW_VERSION);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(device_token, sendCommand);
        logger.info("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void processReceivedData(YandexStationResponse response) {
        if (response.getState() != null) {
            stationState = response.getState();
            if (stationState.aliceState != null) {
                updateState(CHANNEL_STATE_ALICE, new StringType(stationState.aliceState.toString()));
            }
            if (stationState.playing != null) {
                updateState(CHANNEL_STATE_PLAYING, OnOffType.from(stationState.playing));
            }
            if (stationState.volume != null) {
                updateState(CHANNEL_VOLUME, new DecimalType(stationState.volume.doubleValue()));
            }

            processPlayerState(stationState.playerState);
        }
    }

    private void processPlayerState(YandexStationPlayerState playerState) {
        if (playerState != null) {
            if (playerState.getDuration() != null) {
                updateState(CHANNEL_STATE_TRACK_DURATION, new DecimalType(playerState.getDuration()));
            }
            if (playerState.getHasProgressBar() != null && playerState.getHasProgressBar()
                    && playerState.getProgress() != null) {
                updateState(CHANNEL_STATE_TRACK_POSITION, new DecimalType(playerState.getProgress()));
            }
            if (playerState.getPlaylistId() != null) {
                updateState(CHANNEL_STATE_TRACK_PLAYLIST_ID, new StringType(playerState.getPlaylistId()));
            }
            if (playerState.getId() != null) {
                updateState(CHANNEL_STATE_TRACK_ID, new DecimalType(playerState.getId()));
            }
            if (playerState.getPlaylistType() != null) {
                updateState(CHANNEL_STATE_PLAYLIST_TYPE, new StringType(playerState.getPlaylistType()));
            }
            if (playerState.getSubtitle() != null) {
                updateState(CHANNEL_STATE_TRACK_SUBTITLE, new StringType(playerState.getSubtitle()));
            }
            if (playerState.getTitle() != null) {
                updateState(CHANNEL_STATE_TRACK_TITLE, new StringType(playerState.getTitle()));
            }
            if (playerState.getType() != null) {
                updateState(CHANNEL_STATE_TRACK_TYPE, new StringType(playerState.getType()));
            }
            if (playerState.getExtra() != null) {
                if (playerState.getExtra().coverURI != null) {
                    updateState(CHANNEL_STATE_TRACK_COVER_URI, new StringType(playerState.getExtra().coverURI));
                }
            }
            if (playerState.getEntityInfo() != null) {
                if (playerState.getEntityInfo().next != null && !playerState.getEntityInfo().next.isEmpty()
                        && playerState.getEntityInfo().next.containsKey("id")) {
                    String sId = playerState.getEntityInfo().next.get("id");
                    if (sId != null) {
                        updateState(CHANNEL_STATE_TRACK_NEXT_ID, new DecimalType(DecimalType.valueOf(sId)));
                    }
                }
                if (playerState.getEntityInfo().prev != null && !playerState.getEntityInfo().prev.isEmpty()
                        && playerState.getEntityInfo().prev.containsKey("id")) {
                    String sId = playerState.getEntityInfo().prev.get("id");
                    if (sId != null) {
                        updateState(CHANNEL_STATE_TRACK_PREV_ID, new DecimalType(DecimalType.valueOf(sId)));
                    }
                }
            }
        }
    }
    @Override
    protected void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }
}
