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

package org.openhab.binding.yandexstation.internal.response;

import java.util.Map;

/**
 * The {@link YandexStationPlayerState} is describing Player State Entity
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationPlayerState {
    private Double duration;
    private Boolean hasNext;
    private Boolean hasPause;
    private Boolean hasPlay;
    private Boolean hasPrev;
    private Boolean hasProgressBar;
    private Long id;
    private String playerType;
    private String playlistId;
    private String playlistType;
    private Double progress;
    private String subtitle;
    private String title;
    private String type;
    private YandexStationPlayerStateExtra extra;
    private YandexStationPlayerEntity entityInfo;

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Boolean getHasPause() {
        return hasPause;
    }

    public void setHasPause(Boolean hasPause) {
        this.hasPause = hasPause;
    }

    public Boolean getHasPlay() {
        return hasPlay;
    }

    public void setHasPlay(Boolean hasPlay) {
        this.hasPlay = hasPlay;
    }

    public Boolean getHasPrev() {
        return hasPrev;
    }

    public void setHasPrev(Boolean hasPrev) {
        this.hasPrev = hasPrev;
    }

    public Boolean getHasProgressBar() {
        return hasProgressBar;
    }

    public void setHasProgressBar(Boolean hasProgressBar) {
        this.hasProgressBar = hasProgressBar;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayerType() {
        return playerType;
    }

    public void setPlayerType(String playerType) {
        this.playerType = playerType;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getPlaylistType() {
        return playlistType;
    }

    public void setPlaylistType(String playlistType) {
        this.playlistType = playlistType;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public YandexStationPlayerStateExtra getExtra() {
        return extra;
    }

    public void setExtra(YandexStationPlayerStateExtra extra) {
        this.extra = extra;
    }

    public YandexStationPlayerEntity getEntityInfo() {
        return entityInfo;
    }

    public void setEntityInfo(YandexStationPlayerEntity entityInfo) {
        this.entityInfo = entityInfo;
    }

    public class YandexStationPlayerEntity {
        public String id;
        public String repeatMode;
        public String type;
        public Map<String, String> next;
        public Map<String, String> prev;
    }

    public class YandexStationPlayerStateExtra {
        public String coverURI;
        public String requestID;
        public String stateType;
    }
}
