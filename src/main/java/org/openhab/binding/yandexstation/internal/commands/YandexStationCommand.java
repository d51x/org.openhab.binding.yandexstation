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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link YandexStationCommand} is responsible for YandexStationCommand action, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationCommand {
    private String command;
    private Integer position;
    private Double volume;
    private String text;
    @SerializedName("serverActionEventPayload")
    private ServerActionEvent serverActionEvent;

    @Expose(serialize = false)
    private Object extra;

    public void setCommand(YandexStationCommandTypes command) {
        this.command = command.getCommand();
        this.extra = command.getExtra();
    }

    public YandexStationCommand(YandexStationCommandTypes command, Object value) {
        this.command = command.getCommand();
        if (command == YandexStationCommandTypes.CMD_REWIND) {
            this.position = (Integer) value;
        } else if (command == YandexStationCommandTypes.CMD_SET_VOLUME) {
            this.volume = (Double) value;
        } else if (command == YandexStationCommandTypes.CMD_SENT_TEXT) {
            this.text = (String) value;
        } else if (command == YandexStationCommandTypes.CMD_SERVER_ACTION) {
            this.serverActionEvent = (ServerActionEvent) value;
        }
    }

    public YandexStationCommand(YandexStationCommandTypes command) {
        this.command = command.getCommand();
    }

    public YandexStationCommand() {
    }
}
