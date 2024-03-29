/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.QuasarApi;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiImpl;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexSession;
import org.openhab.binding.yandexstation.internal.yandexapi.response.ApiDeviceResponse;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexStationBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationBridge extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(YandexStationBridge.class);
    /**
     * The Api (Glagol).
     */
    public YandexApiImpl api;

    /**
     * The Api Online (Quasar).
     */
    public QuasarApi quasarApi;

    /**
     * The Devices list.
     */
    List<ApiDeviceResponse> devicesList;
    /**
     * The Config.
     */
    public @Nullable YandexStationConfiguration config;

    /**
     * Instantiates a new Yandex station bridge.
     *
     * @param bridge the bridge
     * @param apiFactory the api factory
     * @throws ApiException the api exception
     */
    public YandexStationBridge(Bridge bridge, YandexApiFactory apiFactory) throws ApiException {
        super(bridge);
        api = (YandexApiImpl) apiFactory.getApi();
        quasarApi = (QuasarApi) apiFactory.getApiOnline(this.getThing().getUID().getId());
    }

    @Override
    public void handleRemoval() {
        logger.debug("{} removing ...", getThing().getLabel());
        super.handleRemoval();
        quasarApi.deleteCookieFile();
        quasarApi.deleteCaptchaFile();
        quasarApi.deleteSessionFile();
        quasarApi.deleteXTokenFile();
        quasarApi.deleteMusicTokenFile();
        quasarApi.deleteCsrfTokenFile();
        quasarApi.deleteScenariosFile();
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        logger.debug("thing updateConfiguration");
        // if (configuration.containsKey("cookies")) {
        // Object cookies = configuration.get("cookies");
        // if (cookies == null) {
        // quasarApi.deleteCookieFile();
        // }
        // }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        logger.info("thing handleConfigurationUpdate");
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.debug("{} disabled", getThing().getLabel());
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        YandexStationDiscoveryService.yandexTokenBridgeBusList.add(this);
        config = getConfigAs(YandexStationConfiguration.class);
        if (config != null) {
            try {
                YandexSession yaSession = quasarApi.createSession(config.username, config.password, config.cookies);
                if (!yaSession.musicToken.isEmpty()) {
                    config.yandex_token = yaSession.musicToken;
                    updateStatus(ThingStatus.ONLINE);
                    devicesList = api.getDevices(config.yandex_token);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Can not find Yandex music token");
                }
            } catch (ApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Check bridge configuration");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Gets devices.
     *
     * @return the devices
     */
    public List<ApiDeviceResponse> getDevices() {
        return devicesList;
    }
}
