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
package org.openhab.binding.yandexstation.internal.yandexapi.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ApiDeviceResponse} is describing api response with devices info.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class ApiDeviceResponse extends ApiSuccessResponse {
    /**
     * The Config.
     */
    public Config config;
    /**
     * The Glagol.
     */
    public Glagol glagol;
    /**
     * The Id.
     */
    public String id;
    /**
     * The Name.
     */
    public String name;
    /**
     * The Network info.
     */
    public NetworkInfo networkInfo;
    /**
     * The Platform.
     */
    public String platform;

    /**
     * The type Config.
     */
    public class Config {
        /**
         * The Name.
         */
        public String name;
    }

    /**
     * The type Glagol.
     */
    public class Glagol {
        /**
         * The Security.
         */
        public Security security;

        /**
         * The type Security.
         */
        public class Security {
            /**
             * The Server certificate.
             */
            @SerializedName("server_certificate")
            public String serverCertificate;
            /**
             * The Server private key.
             */
            @SerializedName("server_private_key")
            public String serverPrivateKey;
        }
    }

    /**
     * The type Network info.
     */
    public class NetworkInfo {
        /**
         * The Port.
         */
        @SerializedName("external_port")
        public Integer port;
        /**
         * The Ip adresses.
         */
        @SerializedName("ip_addresses")
        public List<String> ipAdresses = new ArrayList<>();
        /**
         * The Wifi ssid.
         */
        @SerializedName("wifi_ssid")
        public String wifiSSID;
    }
}
