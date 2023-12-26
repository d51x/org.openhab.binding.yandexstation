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

import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_COMMAND_TTS;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_COMMAND_VOICE;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_PLAYER_CONTROL;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_ALICE;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_PLAYING;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_PLAYLIST_TYPE;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_SOFTWARE;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_COVER_URI;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_DURATION;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_ID;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_NEXT_ID;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_PLAYLIST_ID;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_POSITION;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_PREV_ID;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_SUBTITLE;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_TITLE;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_STATE_TRACK_TYPE;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_VOLUME;
import static org.openhab.binding.yandexstation.internal.YandexStationChannels.CHANNEL_VOLUME_CONTROL;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_NEXT;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_PING;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_PLAY;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_PREV;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_REWIND;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_SENT_TEXT;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_SERVER_ACTION;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_SET_VOLUME;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_STOP;
import static org.openhab.binding.yandexstation.internal.commands.YandexStationCommandTypes.CMD_SW_VERSION;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yandexstation.internal.actions.things.YandexStationThingActions;
import org.openhab.binding.yandexstation.internal.commands.FormUpdate;
import org.openhab.binding.yandexstation.internal.commands.FormUpdateSlot;
import org.openhab.binding.yandexstation.internal.commands.ServerActionEvent;
import org.openhab.binding.yandexstation.internal.commands.ServerActionPayload;
import org.openhab.binding.yandexstation.internal.commands.YandexStationCommand;
import org.openhab.binding.yandexstation.internal.commands.YandexStationSendPacket;
import org.openhab.binding.yandexstation.internal.dto.YandexStationPlayerState;
import org.openhab.binding.yandexstation.internal.dto.YandexStationResponse;
import org.openhab.binding.yandexstation.internal.dto.YandexStationState;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiImpl;
import org.openhab.binding.yandexstation.internal.yandexapi.response.ApiDeviceResponse;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link YandexStationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexStationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(YandexStationHandler.class);
    private @Nullable ScheduledFuture<?> refreshPollingJob;
    private @Nullable YandexStationConfiguration config;
    private @Nullable Future<?> initJob;
    private WebSocketClient webSocketClient = new WebSocketClient();
    private YandexStationWebsocket yandexStationWebsocket = new YandexStationWebsocket();
    private ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
    private @Nullable URI websocketAddress;
    private @Nullable Future<Session> webSocketSession;
    private @Nullable YandexApiImpl api;

    private Boolean isConnected = false;

    /**
     * The Yandex station bridge.
     */
    @Nullable
    YandexStationBridge yandexStationBridge;
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

    private Integer prevVolume = 0;

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
                sendSetVolumeCommand(((DecimalType) command).intValue());
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
        logger.debug("Initialize Yandex Station Binding");
        config = getConfigAs(YandexStationConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        yandexStationBridge = getBridgeHandler();
        if (yandexStationBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Check bridge");
        } else {
            receiveDeviceToken();
            logger.info("Connect to Yandex Station: {} with IP {}", config.device_id, config.hostname);
            initJob = connect(config.reconnectInterval);
            if (refreshPollingJob == null || refreshPollingJob.isCancelled()) {
                refreshPollingJob = scheduler.scheduleWithFixedDelay(
                        () -> ping(Objects.requireNonNull(config).device_token), 1, 1, TimeUnit.MINUTES);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (refreshPollingJob != null && !refreshPollingJob.isCancelled()) {
            refreshPollingJob.cancel(true);
            refreshPollingJob = null;
        }
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

    private void setWebSocketConnected(boolean connected) {
        isConnected = connected;
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
                connectStation(config);
                logger.debug("Yandex Station connected: {}", isConnected);
                if (isConnected) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    reconnectWebsocket();
                }
            }
        }, wait, TimeUnit.SECONDS);
    }

    private void connectStation(@Nullable YandexStationConfiguration config) {
        try {
            websocketAddress = new URI("wss://" + config.hostname + ":" + config.port);
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Initialize web socket failed: " + e.getMessage());
            setWebSocketConnected(false);
        }

        webSocketClient.getSslContextFactory().setTrustAll(true);
        yandexStationWebsocket.addMessageHandler(new YandexStationWebsocketInterface() {
            @Override
            public void onConnect(boolean connected) {
                setWebSocketConnected(connected);
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
                setWebSocketConnected(false);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Connection closed: " + statusCode + " - " + reason);

                if (statusCode == 4000) {
                    receiveDeviceToken();
                }
                reconnectWebsocket();
            }

            @Override
            public void onMessage(String data) {
                logger.trace("Data received: {}", data);
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
            webSocketSession = webSocketClient.connect(yandexStationWebsocket, websocketAddress, clientUpgradeRequest);
        } catch (Exception e) {
            logger.error("Connection error {}", e.getMessage());
            setWebSocketConnected(false);
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

    /**
     * Send voice command.
     *
     * @param text the text
     */
    public void sendVoiceCommand(String text) {
        logger.debug("sendVoiceCommand");
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SENT_TEXT, text);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    /**
     * Send tts command.
     *
     * @param text the text
     */
    public void sendTtsCommand(String text) {
        logger.debug("sendTtsCommand");
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

    /**
     * Send stop listening.
     */
    public void sendStopListening() {
        logger.debug("sendStopListening");
        ServerActionEvent event = new ServerActionEvent("on_suggest", null);
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SERVER_ACTION, event);

        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    /**
     * Send set volume command.
     *
     * @param volume the volume
     */
    public void sendSetVolumeCommand(Integer volume) {
        logger.debug("sendSetVolumeCommand");
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_SET_VOLUME, volume);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void sendTrackPositionCommand(Integer position) {
        logger.debug("sendTrackPositionCommand");
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_REWIND, position);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    /**
     * Volume mute.
     */
    public void volumeMute() {
        if (prevVolume > 0) {
            stationState.setVolume(prevVolume);
        } else {
            prevVolume = stationState.getVolume();
            stationState.setVolume(0);
        }
        sendSetVolumeCommand(stationState.getVolume());
    }

    /**
     * Volume up.
     */
    public void volumeUp() {
        Integer volume = stationState.getVolume();
        if (volume < 10) {
            volume++;
            sendSetVolumeCommand(volume);
        }
    }

    /**
     * Volume down.
     */
    public void volumeDown() {
        Integer volume = stationState.getVolume();
        if (volume > 0) {
            volume--;
            sendSetVolumeCommand(volume);
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

    /**
     * Send play next command.
     */
    public void sendPlayNextCommand() {
        logger.debug("sendPlayNextCommand");
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_NEXT);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    /**
     * Send play prev command.
     */
    public void sendPlayPrevCommand() {
        logger.debug("sendPlayPrevCommand");
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PREV);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    /**
     * Send play command.
     */
    public void sendPlayCommand() {
        logger.debug("sendPlayCommand");
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PLAY);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    /**
     * Send stop command.
     */
    public void sendStopCommand() {
        logger.debug("sendStopCommand");
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_STOP);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void ping(String device_token) {
        logger.debug("ping");
        YandexStationCommand sendCommand = new YandexStationCommand(CMD_PING);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(device_token, sendCommand);
        logger.debug("Send packet: {}", yandexPacket);
        yandexStationWebsocket.sendMessage(yandexPacket.toString());
    }

    private void requestSoftwareVersion(String device_token) {
        logger.debug("requestSoftwareVersion");
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
            if (stationState.getVolume() != null) {
                updateState(CHANNEL_VOLUME.getName(), new PercentType(stationState.getVolume()));
            }
            if (stationState.playing != null) {
                updateState(CHANNEL_STATE_PLAYING.getName(), OnOffType.from(stationState.playing));
            }
            if (stationState.playerState != null) {
                processPlayerState(stationState.playerState);
            }
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
            updateState(CHANNEL_STATE_TRACK_ID.getName(), new StringType(playerState.getId()));
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
                updateState(CHANNEL_STATE_TRACK_COVER_URI.getName(),
                        new StringType("https://" + playerState.getExtra().coverURI));
            }
        }
        if (playerState.getEntityInfo() != null) {
            if (playerState.getEntityInfo().next != null && !playerState.getEntityInfo().next.isEmpty()
                    && playerState.getEntityInfo().next.containsKey("id")) {
                String sId = playerState.getEntityInfo().next.get("id");
                if (sId != null && !sId.isEmpty()) {
                    updateState(CHANNEL_STATE_TRACK_NEXT_ID.getName(), new StringType(sId));
                }
            }
            if (playerState.getEntityInfo().prev != null && !playerState.getEntityInfo().prev.isEmpty()
                    && playerState.getEntityInfo().prev.containsKey("id")) {
                String sId = playerState.getEntityInfo().prev.get("id");
                if ((sId != null && !sId.isEmpty()) && (!sId.isEmpty())) {
                    updateState(CHANNEL_STATE_TRACK_PREV_ID.getName(), new StringType(sId));
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
            ApiDeviceResponse device = api.findDevice(config.device_id, yandexStationBridge.config.yandex_token);
            String token = api.getDeviceToken(yandexStationBridge.config.yandex_token, config.device_id,
                    device.platform);

            config.platform = device.platform;
            config.device_token = token;

            logger.info("Yandex station IP's: {}", device.networkInfo.ipAdresses);

            // config.hostname = device.networkInfo.ipAdresses.get(0);
            config.hostname = Objects.requireNonNull(device.networkInfo.ipAdresses.stream()
                    .filter(ip -> !ip.startsWith("169.254") && !ip.contains(":"))
                    .findFirst().orElse(null));
            config.port = String.valueOf(device.networkInfo.port);

            Configuration configuration = thing.getConfiguration();
            configuration.put("device_token", token);
            configuration.put("platform", device.platform);
            configuration.put("hostname", config.hostname);
            configuration.put("port", config.port);
            configuration.put("server_certificate", device.glagol.security.serverCertificate);
            configuration.put("server_private_key", device.glagol.security.serverPrivateKey);
            updateConfiguration(configuration);

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
        // properties.put("IP Address:", device.networkInfo.ipAdresses.get(0));
        properties.put("IP Address:", Objects.requireNonNull(device.networkInfo.ipAdresses.stream()
                .filter(ip -> !ip.startsWith("169.254") && !ip.contains(":"))
                .findFirst().orElse(null)));
        properties.put("Platform:", device.platform);
        properties.put("Device Name:", YandexStationTypes.getNameByPlatform(device.platform));
        properties.put("Friendly Name:", device.name);
        updateProperties(properties);
    }

    /**
     * Gets station state.
     *
     * @return the station state
     */
    public YandexStationState getStationState() {
        return stationState;
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
}
