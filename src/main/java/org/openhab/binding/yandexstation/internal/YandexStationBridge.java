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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yandexstation.internal.discovery.YandexStationDiscoveryService;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiDeviceResponse;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiImpl;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

/**
 * The {@link YandexStationBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationBridge extends BaseBridgeHandler {
    public YandexApiImpl api;
    List<ApiDeviceResponse> devicesList;
    public Map<String, ApiDeviceResponse> devices;
    public @Nullable YandexStationConfiguration config;

    public YandexStationBridge(Bridge bridge, YandexApiFactory apiFactory) throws ApiException {
        super(bridge);
        api = (YandexApiImpl) apiFactory.getApi();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        YandexStationDiscoveryService.yandexTokenBridgeBusList.add(this);
        config = getConfigAs(YandexStationConfiguration.class);
        try {
            devicesList = api.getDevices(config.yandex_token);
            updateStatus(ThingStatus.ONLINE);
        } catch (ApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Can not connect to Yandex");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public List<ApiDeviceResponse> getDevices() {
        return devicesList;
    }
}
