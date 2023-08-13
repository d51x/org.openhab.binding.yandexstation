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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link YandexStationBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexStationBindingConstants {

    /**
     * The constant BINDING_ID.
     */
    public static final String BINDING_ID = "yandexstation";
    /**
     * The constant THING_TYPE_ID.
     */
    public static final String THING_TYPE_ID = "station";
    /**
     * The constant BRIDGE_TYPE_ID.
     */
    public static final String BRIDGE_TYPE_ID = "bridge";

    /**
     * The constant THING_TYPE_STATION.
     */
    public static final ThingTypeUID THING_TYPE_STATION = new ThingTypeUID(BINDING_ID, THING_TYPE_ID);
    /**
     * The constant THING_TYPE_BRIDGE.
     */
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_ID);
    /**
     * The constant CHANNEL_GROUP_STATION.
     */
    public static final String CHANNEL_GROUP_STATION = "station";
    /**
     * The constant CHANNEL_GROUP_SPEECH.
     */
    public static final String CHANNEL_GROUP_SPEECH = "speech";
    /**
     * The constant CHANNEL_GROUP_VOLUME.
     */
    public static final String CHANNEL_GROUP_VOLUME = "volume";
    /**
     * The constant CHANNEL_GROUP_PLAYER_PROPERTIES.
     */
    public static final String CHANNEL_GROUP_PLAYER_PROPERTIES = "player-properties";
    /**
     * The constant CHANNEL_GROUP_PLAYER_CONTROL.
     */
    public static final String CHANNEL_GROUP_PLAYER_CONTROL = "player-control";
    /**
     * The constant CHANNEL_GROUP_TRACK.
     */
    public static final String CHANNEL_GROUP_TRACK = "track-info";

    /**
     * The constant CHANNEL_COMMAND_CHANGE_VOLUME.
     */
    public static final String CHANNEL_COMMAND_CHANGE_VOLUME = "volume_change";

    /**
     * The constant WSS_PORT.
     */
    public static final String WSS_PORT = "1961";
}
