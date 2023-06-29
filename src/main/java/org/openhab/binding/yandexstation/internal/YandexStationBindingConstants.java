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

    private static final String BINDING_ID = "yandexstation";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "station");

    // List of all Channel ids
    public static final String CHANNEL_COMMAND_VOICE = "voice";
    public static final String CHANNEL_COMMAND_TTS = "tts";

    public static final String CHANNEL_COMMAND_CHANGE_VOLUME = "volume_change";
    public static final String CHANNEL_STATE_ALICE = "aliceState";
    public static final String CHANNEL_STATE_PLAYING = "playing";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_STATE_TRACK_DURATION = "duration";
    public static final String CHANNEL_STATE_TRACK_POSITION = "progress";
    public static final String CHANNEL_STATE_TRACK_PLAYLIST_ID = "playlistId";
    public static final String CHANNEL_STATE_TRACK_ID = "trackId";
    public static final String CHANNEL_STATE_PLAYLIST_TYPE = "playlistType";
    public static final String CHANNEL_STATE_TRACK_SUBTITLE = "subtitle";
    public static final String CHANNEL_STATE_TRACK_TITLE = "title";
    public static final String CHANNEL_STATE_TRACK_TYPE = "trackType";
    public static final String CHANNEL_STATE_TRACK_COVER_URI = "coverURI";
    public static final String CHANNEL_STATE_TRACK_NEXT_ID = "nextId";
    public static final String CHANNEL_STATE_TRACK_PREV_ID = "prevId";
    public static final String CHANNEL_PLAYER_CONTROL = "player_control";
    public static final String CHANNEL_VOLUME_CONTROL = "volume_control";

    public static final String WSS_PORT = "1961";

}
