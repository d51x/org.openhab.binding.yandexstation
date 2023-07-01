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

import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.WSS_PORT;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link YandexStationConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexStationConfiguration {
    public String hostname = "";
    public String port = WSS_PORT;
    public String platform = "";
    public String device_token = "";
    public String device_id = "";
    public String yandex_token = "";

    public int reconnectInterval = 60;

    public String server_certificate = "";
    public String server_private_key = "";
}
