package org.openhab.binding.yandexstation.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yandexstation.internal.discovery.YandexStationDiscoveryService;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiDeviceResponse;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiImpl;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

import java.util.List;

public class YandexStationBridge extends BaseBridgeHandler {
    public YandexApiImpl api;
    List<ApiDeviceResponse> devicesList;
    private @Nullable YandexStationConfiguration config;
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
        } catch (ApiException e) {

        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    public List<ApiDeviceResponse> getDevices() {
        return devicesList;
    }
}
