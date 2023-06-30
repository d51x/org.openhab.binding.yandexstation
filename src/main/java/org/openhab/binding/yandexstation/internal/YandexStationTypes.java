
/*
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information.
 *
 * This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.yandexstation.internal;

import java.util.Arrays;

/**
 * The {@link YandexStationTypes} is responsible for handling Yandex Station Type.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public enum YandexStationTypes {
    PLATFORM_YANDEX_STATION_1("yandexstation", "Яндекс Станция", true),
    PLATFORM_YANDEX_STATION_MAX("yandexstation", "Яндекс Станция Макс", true),
    PLATFORM_YANDEX_STATION_2("yandexmidi", "Яндекс Станция 2", true),
    PLATFORM_YANDEX_STATION_MINI("yandexmini", "Яндекс Станция Мини", true),
    PLATFORM_YANDEX_STATION_MINI_2("yandexmini_2", "Яндекс Станция Мини 2", true),
    PLATFORM_YANDEX_STATION_LITE("yandexmicro", "Яндекс Станция Лайт", true),
    PLATFORM_YANDEX_MODULE("yandexmodule", "Яндекс Модуль", true),
    PLATFORM_YANDEX_MODULE_2("yandexmodule_2", "Яндекс Модуль 2", true),
    PLATFORM_YANDEX_TV("yandex_tv", "Яндекс ТВ", true),
    PLATFORM_JBL_LINK_MUSIC("jbl_link_music", "JBL Link MusicJBL Link Music", true),
    PLATFORM_JBL_LINK_PORTABLE("jbl_link_portable", "JBL Link Portable", true),

    PLATFORM_DEXP("lightcomm", "DEXP Smartbox", false),
    PLATFORM_ELARI("elari_a98", "Elari SmartBeat", false),
    PLATFORM_IRBIS("linkplay_a98", "IRBIS A", false),
    PLATFORM_LG_XBOOM("wk7y", "LG XBOOM AI ThinQ WK7Y", false),
    PLATFORM_PRESTIGIO("prestigio_smart_mate", "Prestigio Smartmate", false),
    PLATFORM_UNKNOWN("unknown", "Неизвестное устройтсов", false),

    ;

    private String platform;
    private String name;
    private Boolean localApi;

    YandexStationTypes(String platform, String name, Boolean localApi) {
        this.platform = platform;
        this.name = name;
        this.localApi = localApi;
    }

    public String getPlatform() {
        return platform;
    }

    public String getName() {
        return name;
    }

    public Boolean getLocalApi() {
        return localApi;
    }

    public static String getNameByPlatform(String platform) {
        return Arrays.stream(YandexStationTypes.values()).filter(v -> v.getPlatform().equals(platform)).findFirst()
                .orElse(PLATFORM_UNKNOWN).getName();
    }

    public static Boolean isLocalApi(String platform) {
        return Arrays.stream(YandexStationTypes.values()).filter(v -> v.getPlatform().equals(platform)).findFirst()
                .orElse(PLATFORM_UNKNOWN).getLocalApi();
    }
}
