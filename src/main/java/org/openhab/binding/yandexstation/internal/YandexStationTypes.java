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

import java.util.Arrays;

/**
 * The {@link YandexStationTypes} is responsible for handling Yandex Station Type.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public enum YandexStationTypes {
    /**
     * Platform yandex station 1 yandex station types.
     */
    PLATFORM_YANDEX_STATION_1("yandexstation", "Яндекс Станция", true),
    /**
     * Platform yandex station max yandex station types.
     */
    PLATFORM_YANDEX_STATION_MAX("yandexstation_2", "Яндекс Станция Макс", true),
    PLATFORM_YANDEX_STATION_DUO_MAX("chiron", "Яндекс Станция Дуо Макс", true),
    /**
     * Platform yandex station 2 yandex station types.
     */
    PLATFORM_YANDEX_STATION_2("yandexmidi", "Яндекс Станция 2", true),
    PLATFORM_YANDEX_STATION_MIDI("cucumber", "Станция Миди", true),
    /**
     * Platform yandex station mini yandex station types.
     */
    PLATFORM_YANDEX_STATION_MINI("yandexmini", "Яндекс Станция Мини", true),
    /**
     * Platform yandex station mini 2 yandex station types.
     */
    PLATFORM_YANDEX_STATION_MINI_2("yandexmini_2", "Яндекс Станция Мини 2", true),
    /**
     * Platform yandex station lite yandex station types.
     */
    PLATFORM_YANDEX_STATION_LITE("yandexmicro", "Яндекс Станция Лайт", true),
    /**
     * Platform yandex module yandex station types.
     */
    PLATFORM_YANDEX_MODULE("yandexmodule", "Яндекс Модуль", true),
    /**
     * Platform yandex module 2 yandex station types.
     */
    PLATFORM_YANDEX_MODULE_2("yandexmodule_2", "Яндекс Модуль 2", true),
    /**
     * Platform yandex tv yandex station types.
     */
    PLATFORM_YANDEX_TV("yandex_tv", "Яндекс ТВ", true),
    /**
     * The Platform jbl link music.
     */
    PLATFORM_JBL_LINK_MUSIC("jbl_link_music", "JBL Link MusicJBL Link Music", true),
    /**
     * The Platform jbl link portable.
     */
    PLATFORM_JBL_LINK_PORTABLE("jbl_link_portable", "JBL Link Portable", true),

    /**
     * The Platform dexp.
     */
    PLATFORM_DEXP("lightcomm", "DEXP Smartbox", false),
    /**
     * The Platform elari.
     */
    PLATFORM_ELARI("elari_a98", "Elari SmartBeat", false),
    /**
     * The Platform irbis.
     */
    PLATFORM_IRBIS("linkplay_a98", "IRBIS A", false),
    /**
     * The Platform lg xboom.
     */
    PLATFORM_LG_XBOOM("wk7y", "LG XBOOM AI ThinQ WK7Y", false),
    /**
     * The Platform prestigio.
     */
    PLATFORM_PRESTIGIO("prestigio_smart_mate", "Prestigio Smartmate", false),
    PLATFORM_DISPLAY_XIAOMI("quinglong", "Smart Display 10R X10G", false),
    PLATFORM_ZIGBEE_HUB("saturn", "Yandex Hub", false),
    /**
     * Platform unknown yandex station types.
     */
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

    /**
     * Gets platform.
     *
     * @return the platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets local api.
     *
     * @return the local api
     */
    public Boolean getLocalApi() {
        return localApi;
    }

    /**
     * Gets name by platform.
     *
     * @param platform the platform
     * @return the name by platform
     */
    public static String getNameByPlatform(String platform) {
        return Arrays.stream(YandexStationTypes.values()).filter(v -> v.getPlatform().equals(platform)).findFirst()
                .orElse(PLATFORM_UNKNOWN).getName();
    }

    /**
     * Is local api boolean.
     *
     * @param platform the platform
     * @return the boolean
     */
    public static Boolean isLocalApi(String platform) {
        return Arrays.stream(YandexStationTypes.values())
                .filter(v -> v.getPlatform().equals(platform) || platform.contains(v.getPlatform())).findFirst()
                .orElse(PLATFORM_UNKNOWN).getLocalApi();
    }
}
