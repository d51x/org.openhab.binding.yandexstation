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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link YandexStationWebsocketInterface} is responsible for interfacing the Websocket.
 *
 * @author Dmitry P. (d51x) - Initial contribution
 */
@NonNullByDefault
public interface YandexStationWebsocketInterface {
    /**
     * On connect.
     *
     * @param connected the connected
     */
    public void onConnect(boolean connected);

    /**
     * On close.
     *
     * @param statusCode the status code
     * @param reason the reason
     * @throws Exception the exception
     */
    public void onClose(int statusCode, String reason) throws Exception;

    /**
     * On message.
     *
     * @param data the data
     */
    public void onMessage(String data);

    /**
     * On error.
     *
     * @param cause the cause
     */
    public void onError(Throwable cause);
}
