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

    public static final String BINDING_ID = "yandexstation";
    public static final String THING_TYPE_ID = "station";
    public static final String BRIDGE_TYPE_ID = "bridge";

    public static final ThingTypeUID THING_TYPE_STATION = new ThingTypeUID(BINDING_ID, THING_TYPE_ID);
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_ID);
    public static final String CHANNEL_GROUP_STATION = "station";
    public static final String CHANNEL_GROUP_SPEECH = "speech";
    public static final String CHANNEL_GROUP_VOLUME = "volume";
    public static final String CHANNEL_GROUP_PLAYER_PROPERTIES = "player-properties";
    public static final String CHANNEL_GROUP_PLAYER_CONTROL = "player-control";
    public static final String CHANNEL_GROUP_TRACK = "track-info";

    public static final String CHANNEL_COMMAND_CHANGE_VOLUME = "volume_change";

    public static final String WSS_PORT = "1961";
}
