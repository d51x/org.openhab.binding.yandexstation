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
package org.openhab.binding.yandexstation.internal.commands;

/**
 * The {@link YandexStationCommandTypes} is responsible for YandexStationCommandTypes action, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public enum YandexStationCommandTypes {
    /**
     * Cmd ping yandex station command types.
     */
    CMD_PING("ping"),
    /**
     * Cmd play yandex station command types.
     */
    CMD_PLAY("play"),
    /**
     * Cmd stop yandex station command types.
     */
    CMD_STOP("stop"),
    /**
     * Cmd prev yandex station command types.
     */
    CMD_PREV("prev"),
    /**
     * Cmd next yandex station command types.
     */
    CMD_NEXT("next"),
    /**
     * Cmd sw version yandex station command types.
     */
    CMD_SW_VERSION("softwareVersion"),
    /**
     * Cmd rewind yandex station command types.
     */
    CMD_REWIND("rewind", "position"),
    /**
     * Cmd set volume yandex station command types.
     */
    CMD_SET_VOLUME("setVolume", "volume"),
    /**
     * Cmd sent text yandex station command types.
     */
    CMD_SENT_TEXT("sendText", "text"),
    /**
     * Cmd server action yandex station command types.
     */
    CMD_SERVER_ACTION("serverAction", "serverActionEventPayload");

    private String command;
    private String extra;

    YandexStationCommandTypes(String command) {
        this.command = command;
        this.extra = null;
    }

    YandexStationCommandTypes(String command, String extra) {
        this.command = command;
    }

    /**
     * Gets command.
     *
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets extra.
     *
     * @return the extra
     */
    public String getExtra() {
        return extra;
    }
}
