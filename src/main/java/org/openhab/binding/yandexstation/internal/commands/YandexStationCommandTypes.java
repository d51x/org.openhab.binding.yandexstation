/*
 *  Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 */

package org.openhab.binding.yandexstation.internal.commands;

/**
 * The {@link YandexStationCommandTypes} is responsible for YandexStationCommandTypes action, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public enum YandexStationCommandTypes {
    CMD_PING("ping"),
    CMD_PLAY("play"),
    CMD_STOP("stop"),
    CMD_PREV("prev"),
    CMD_NEXT("next"),
    CMD_SW_VERSION("softwareVersion"),
    CMD_REWIND("rewind", "position"),
    CMD_SET_VOLUME("setVolume", "volume"),
    CMD_SENT_TEXT("sendText", "text"),
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

    public String getCommand() {
        return command;
    }

    public String getExtra() {
        return extra;
    }
}
