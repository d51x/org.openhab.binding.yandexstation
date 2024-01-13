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

package org.openhab.binding.yandexstation.internal.yandexapi;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link YandexSession} is describing YandexSession.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexSession {
    public String login = "";
    public String password = "";
    public String csrfToken = "";
    public String xToken = "";
    public String musicToken = "";
    public String trackId = "";

    public YandexSession() {
    }

    public YandexSession(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
