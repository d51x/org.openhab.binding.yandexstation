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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.yandexstation.internal.commands.*;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
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

    public YandexStationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_VOICE_COMMAND.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                // TODO: handle data refresh
                sendVoiceCommand(command.toString());
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        } else if (CHANNEL_TTS_COMMAND.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                sendTtsCommand(command.toString());
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(YandexStationConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        initJob = scheduler.schedule(() -> {
            if (config == null) {
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR);
            } else {
                boolean thingReachable = connectStation(config);

                // when done do:
                if (thingReachable) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }, 1, TimeUnit.SECONDS);

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
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
                    logger.info("websocket connected");

                    YandexStationCommand sendCommand = new YandexStationCommand(CMD_PING);
                    YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendCommand);
                    logger.info("Send packet: {}", yandexPacket);
                    yandexStationWebsocket.sendMessage(yandexPacket.toString());

                } else {
                    logger.error("websocket connection failed");
                }
            }

            @Override
            public void onClose() {
                logger.info("Websocket connection closed");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "Connection closed");
                //disposeWebsocketPollingJob();
                reconnectWebsocket();
            }

            @Override
            public void onMessage(String data) {
                logger.debug("Data received: {}", data);
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
            webSocketClient.connect(yandexStationWebsocket, websocketAddress, clientUpgradeRequest);
        } catch (Exception e) {
            logger.error("Connection error {}", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        //super.dispose();

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
        connectStation(config);
    }

    private void sendVoiceCommand(String text) {
        YandexStationCommand sendTextCommand = new YandexStationCommand(CMD_SENT_TEXT, text);
        YandexStationSendPacket yandexPacket = new YandexStationSendPacket(config.device_token, sendTextCommand);
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
}
