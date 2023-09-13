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
package org.openhab.binding.yandexstation.internal.dto;

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
    private String id;
    private String playerType;
    private String playlistId;
    private String playlistType;
    private Double progress;
    private String subtitle;
    private String title;
    private String type;
    private YandexStationPlayerStateExtra extra;
    private YandexStationPlayerEntity entityInfo;

    /**
     * Gets duration.
     *
     * @return the duration
     */
    public Double getDuration() {
        return duration;
    }

    /**
     * Sets duration.
     *
     * @param duration the duration
     */
    public void setDuration(Double duration) {
        this.duration = duration;
    }

    /**
     * Gets has next.
     *
     * @return the has next
     */
    public Boolean getHasNext() {
        return hasNext;
    }

    /**
     * Sets has next.
     *
     * @param hasNext the has next
     */
    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    /**
     * Gets has pause.
     *
     * @return the has pause
     */
    public Boolean getHasPause() {
        return hasPause;
    }

    /**
     * Sets has pause.
     *
     * @param hasPause the has pause
     */
    public void setHasPause(Boolean hasPause) {
        this.hasPause = hasPause;
    }

    /**
     * Gets has play.
     *
     * @return the has play
     */
    public Boolean getHasPlay() {
        return hasPlay;
    }

    /**
     * Sets has play.
     *
     * @param hasPlay the has play
     */
    public void setHasPlay(Boolean hasPlay) {
        this.hasPlay = hasPlay;
    }

    /**
     * Gets has prev.
     *
     * @return the has prev
     */
    public Boolean getHasPrev() {
        return hasPrev;
    }

    /**
     * Sets has prev.
     *
     * @param hasPrev the has prev
     */
    public void setHasPrev(Boolean hasPrev) {
        this.hasPrev = hasPrev;
    }

    /**
     * Gets has progress bar.
     *
     * @return the has progress bar
     */
    public Boolean getHasProgressBar() {
        return hasProgressBar;
    }

    /**
     * Sets has progress bar.
     *
     * @param hasProgressBar the has progress bar
     */
    public void setHasProgressBar(Boolean hasProgressBar) {
        this.hasProgressBar = hasProgressBar;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets player type.
     *
     * @return the player type
     */
    public String getPlayerType() {
        return playerType;
    }

    /**
     * Sets player type.
     *
     * @param playerType the player type
     */
    public void setPlayerType(String playerType) {
        this.playerType = playerType;
    }

    /**
     * Gets playlist id.
     *
     * @return the playlist id
     */
    public String getPlaylistId() {
        return playlistId;
    }

    /**
     * Sets playlist id.
     *
     * @param playlistId the playlist id
     */
    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    /**
     * Gets playlist type.
     *
     * @return the playlist type
     */
    public String getPlaylistType() {
        return playlistType;
    }

    /**
     * Sets playlist type.
     *
     * @param playlistType the playlist type
     */
    public void setPlaylistType(String playlistType) {
        this.playlistType = playlistType;
    }

    /**
     * Gets progress.
     *
     * @return the progress
     */
    public Double getProgress() {
        return progress;
    }

    /**
     * Sets progress.
     *
     * @param progress the progress
     */
    public void setProgress(Double progress) {
        this.progress = progress;
    }

    /**
     * Gets subtitle.
     *
     * @return the subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Sets subtitle.
     *
     * @param subtitle the subtitle
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets extra.
     *
     * @return the extra
     */
    public YandexStationPlayerStateExtra getExtra() {
        return extra;
    }

    /**
     * Sets extra.
     *
     * @param extra the extra
     */
    public void setExtra(YandexStationPlayerStateExtra extra) {
        this.extra = extra;
    }

    /**
     * Gets entity info.
     *
     * @return the entity info
     */
    public YandexStationPlayerEntity getEntityInfo() {
        return entityInfo;
    }

    /**
     * Sets entity info.
     *
     * @param entityInfo the entity info
     */
    public void setEntityInfo(YandexStationPlayerEntity entityInfo) {
        this.entityInfo = entityInfo;
    }

    /**
     * The type Yandex station player entity.
     */
    public class YandexStationPlayerEntity {
        /**
         * The Id.
         */
        public String id;
        /**
         * The Repeat mode.
         */
        public String repeatMode;
        /**
         * The Type.
         */
        public String type;
        /**
         * The Next.
         */
        public Map<String, String> next;
        /**
         * The Prev.
         */
        public Map<String, String> prev;
    }

    /**
     * The type Yandex station player state extra.
     */
    public class YandexStationPlayerStateExtra {
        /**
         * The Cover uri.
         */
        public String coverURI;
        /**
         * The Request id.
         */
        public String requestID;
        /**
         * The State type.
         */
        public String stateType;
    }
}
