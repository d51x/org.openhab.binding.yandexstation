package org.openhab.binding.yandexstation.internal.response;

import java.util.Map;

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
    private String subtitle;  //FENDA
    private String title;  //Sunrise Falling
    private String type;  //Track
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
        public Map<String, String> next;  // id and type
        public Map<String, String> prev;  // id and type
    }
    public class YandexStationPlayerStateExtra {
        public String coverURI;
        public String requestID;
        public String stateType;
    }
}
