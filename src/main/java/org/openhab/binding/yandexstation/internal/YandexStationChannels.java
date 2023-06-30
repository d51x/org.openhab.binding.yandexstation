
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

import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.*;

/**
 * The {@link YandexStationChannels} is responsible for channels definition.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public enum YandexStationChannels {
    CHANNEL_STATE_ALICE("aliceState", CHANNEL_GROUP_STATION),
    CHANNEL_STATE_SOFTWARE("sw_version", CHANNEL_GROUP_STATION),
    CHANNEL_STATE_PLAYING("playing", CHANNEL_GROUP_STATION),
    CHANNEL_VOLUME("volume", CHANNEL_GROUP_STATION),
    CHANNEL_COMMAND_VOICE("voice", CHANNEL_GROUP_SPEECH),
    CHANNEL_COMMAND_TTS("tts", CHANNEL_GROUP_SPEECH),
    CHANNEL_VOLUME_CONTROL("volume_control", CHANNEL_GROUP_VOLUME),
    CHANNEL_STATE_TRACK_DURATION("duration", CHANNEL_GROUP_PLAYER_PROPERTIES),
    CHANNEL_STATE_TRACK_POSITION("progress", CHANNEL_GROUP_PLAYER_PROPERTIES),
    CHANNEL_STATE_TRACK_PLAYLIST_ID("playlistId", CHANNEL_GROUP_PLAYER_PROPERTIES),
    CHANNEL_STATE_PLAYLIST_TYPE("playlistType", CHANNEL_GROUP_PLAYER_PROPERTIES),
    CHANNEL_STATE_TRACK_NEXT_ID("nextId", CHANNEL_GROUP_PLAYER_PROPERTIES),
    CHANNEL_STATE_TRACK_PREV_ID("prevId", CHANNEL_GROUP_PLAYER_PROPERTIES),
    CHANNEL_PLAYER_CONTROL("player_control", CHANNEL_GROUP_PLAYER_CONTROL),
    CHANNEL_STATE_TRACK_ID("trackId", CHANNEL_GROUP_TRACK),
    CHANNEL_STATE_TRACK_SUBTITLE("subtitle", CHANNEL_GROUP_TRACK),
    CHANNEL_STATE_TRACK_TITLE("title", CHANNEL_GROUP_TRACK),
    CHANNEL_STATE_TRACK_TYPE("trackType", CHANNEL_GROUP_TRACK),
    CHANNEL_STATE_TRACK_COVER_URI("coverURI", CHANNEL_GROUP_TRACK);

    private String channelId;
    private String groupId;

    YandexStationChannels(String channelId, String groupId) {
        this.channelId = channelId;
        this.groupId = groupId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return groupId + "#" + channelId;
    }
}
