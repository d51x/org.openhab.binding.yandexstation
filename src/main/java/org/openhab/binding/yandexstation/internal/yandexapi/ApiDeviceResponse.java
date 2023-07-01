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
package org.openhab.binding.yandexstation.internal.yandexapi;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ApiDeviceResponse} is describing api response with devices info.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class ApiDeviceResponse extends ApiSuccessResponse {
    public Config config;
    public Glagol glagol;
    public String id;
    public String name;
    public NetworkInfo networkInfo;
    public String platform;

    public class Config {
        public String name;
    }

    public class Glagol {
        public Security security;

        public class Security {
            @SerializedName("server_certificate")
            public String serverCertificate;
            @SerializedName("server_private_key")
            public String serverPrivateKey;
        }
    }

    public class NetworkInfo {
        @SerializedName("external_port")
        public Integer port;
        @SerializedName("ip_addresses")
        public List<String> ipAdresses = new ArrayList<>();
        @SerializedName("wifi_ssid")
        public String wifiSSID;
    }
}
