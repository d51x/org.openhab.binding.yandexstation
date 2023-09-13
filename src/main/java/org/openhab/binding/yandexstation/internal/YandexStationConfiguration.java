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
    /**
     * The Hostname.
     */
    public String hostname = "";
    /**
     * The Port.
     */
    public String port = WSS_PORT;
    /**
     * The Platform.
     */
    public String platform = "";
    /**
     * The Device token.
     */
    public String device_token = "";
    /**
     * The Device id.
     */
    public String device_id = "";
    /**
     * The Yandex token.
     */
    public String yandex_token = "";

    /**
     * The Reconnect interval.
     */
    public int reconnectInterval = 60;

    /**
     * The Server certificate.
     */
    public String server_certificate = "";
    /**
     * The Server private key.
     */
    public String server_private_key = "";
}
