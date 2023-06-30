/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
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

    public void addMessageHandler(YandexStationWebsocketInterface yandexStationWebsocketInterfaceHandler) {
        this.websocketHandler = yandexStationWebsocketInterfaceHandler;
    }

    @OnWebSocketMessage
    public void onText(Session session, String message) {
        if (websocketHandler != null) {
            websocketHandler.onMessage(message);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        if (websocketHandler != null) {
            websocketHandler.onConnect(true);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.error("YandexStationWebSocketError {}", cause.getMessage());
        if (websocketHandler != null) {
            websocketHandler.onError(cause);
        }
    }

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

    public void sendMessage(String str) {
        if (session != null) {
            try {
                session.getRemote().sendString(str);
            } catch (IOException e) {
                logger.error("YandexStation error sending message to websocket: {}", e.getMessage());
            }
        }
    }

    public void closeWebsocketSession() {
        if (session != null) {
            session.close();
        }
    }
}
