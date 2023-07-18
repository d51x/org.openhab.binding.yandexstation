/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexStationWebsocket} is responsible for the Websocket Connection
 *
 * @author Dmitry P. (d51x) - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class YandexStationWebsocket {

    private @Nullable Session session;
    private final Logger logger = LoggerFactory.getLogger(YandexStationWebsocket.class);
    private @Nullable YandexStationWebsocketInterface websocketHandler;

    /**
     * Add message handler.
     *
     * @param yandexStationWebsocketInterfaceHandler the yandex station websocket interface handler
     */
    public void addMessageHandler(YandexStationWebsocketInterface yandexStationWebsocketInterfaceHandler) {
        this.websocketHandler = yandexStationWebsocketInterfaceHandler;
    }

    /**
     * On text.
     *
     * @param session the session
     * @param message the message
     */
    @OnWebSocketMessage
    public void onText(Session session, String message) {
        if (websocketHandler != null) {
            websocketHandler.onMessage(message);
        }
    }

    /**
     * On connect.
     *
     * @param session the session
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        if (websocketHandler != null) {
            websocketHandler.onConnect(true);
        }
    }

    /**
     * On error.
     *
     * @param cause the cause
     */
    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.error("YandexStationWebSocketError {}", cause.getMessage());
        if (websocketHandler != null) {
            websocketHandler.onError(cause);
        }
    }

    /**
     * On close.
     *
     * @param statusCode the status code
     * @param reason the reason
     * @throws Exception the exception
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) throws Exception {
        if (statusCode != StatusCode.NORMAL) {
            logger.error("YandexStationWebSocket Connection closed: {} - {}", statusCode, reason);
        }

        if (session != null) {
            if (!session.isOpen()) {
                if (session != null) {
                    session.close(statusCode, reason);
                }
            }
            session = null;
        }

        if (websocketHandler != null) {
            websocketHandler.onClose(statusCode, reason);
        }
    }

    /**
     * Send message.
     *
     * @param str the str
     */
    public void sendMessage(String str) {
        if (session != null) {
            try {
                session.getRemote().sendString(str);
            } catch (IOException e) {
                logger.error("YandexStation error sending message to websocket: {}", e.getMessage());
            }
        }
    }

    /**
     * Close websocket session.
     */
    public void closeWebsocketSession() {
        if (session != null) {
            session.close();
        }
    }
}
