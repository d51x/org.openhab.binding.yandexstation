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
 * The {@link YandexStationState} is describing station state entity
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationState {
    /**
     * The Alice state.
     */
    public String aliceState; // IDLE LISTENING SPEAKING BUSY
    /**
     * The Can stop.
     */
    public Boolean canStop;
    /**
     * The Hdmi.
     */
    public Map<String, Boolean> hdmi;
    /**
     * The Playing.
     */
    public Boolean playing;
    /**
     * The Time since last voice activity.
     */
    public Long timeSinceLastVoiceActivity;
    /**
     * The Volume.
     */
    private Double volume;

    /**
     * The Player state.
     */
    public YandexStationPlayerState playerState;

    public Integer getVolume() {
        return (int) (volume.doubleValue() * 10);
    }

    public void setVolume(Integer volume) {
        this.volume = volume / 10.0;
    }
}
