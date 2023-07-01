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
package org.openhab.binding.yandexstation.internal.response;

import java.util.Map;

/**
 * The {@link YandexStationState} is describing station state entity
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationState {
    public String aliceState; // IDLE LISTENING SPEAKING BUSY
    public Boolean canStop;
    public Map<String, Boolean> hdmi;
    public Boolean playing;
    public Long timeSinceLastVoiceActivity;
    public Double volume;

    public YandexStationPlayerState playerState;
}
