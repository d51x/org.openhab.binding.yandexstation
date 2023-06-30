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
 * The {@link ServerActionEvent} is responsible for Server action, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */

public class ServerActionEvent {
    private String type;
    private String name;

    private ServerActionPayload payload;

    public ServerActionEvent(String name, ServerActionPayload payload) {
        this.type = "server_action";
        this.name = name;
        this.payload = payload;
    }
}
