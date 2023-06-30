package org.openhab.binding.yandexstation.internal.yandexapi;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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
