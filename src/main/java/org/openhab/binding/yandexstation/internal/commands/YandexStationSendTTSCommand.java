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
 * The {@link YandexStationSendTTSCommand} is responsible for YandexStationCommandTypes action, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationSendTTSCommand extends YandexStationCommand {

    private Object serverActionEventPayload;

    public YandexStationSendTTSCommand(String text) {
        this.setCommand(YandexStationCommandTypes.CMD_SERVER_ACTION);
    }

}
