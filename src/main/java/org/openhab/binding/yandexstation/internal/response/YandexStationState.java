package org.openhab.binding.yandexstation.internal.response;

import java.util.Map;

public class YandexStationState {
    public String aliceState; //IDLE LISTENING SPEAKING BUSY
    public Boolean canStop;
    public Map<String, Boolean> hdmi;
    public Boolean playing;
    public Long timeSinceLastVoiceActivity;
    public Double volume;

    public YandexStationPlayerState playerState;
}
