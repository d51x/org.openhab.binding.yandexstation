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
package org.openhab.binding.yandexstation.internal;

import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.THING_TYPE_STATION;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yandexstation.internal.yandexapi.ApiException;
import org.openhab.binding.yandexstation.internal.yandexapi.YandexApiFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ThingType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link YandexStationHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.yandexstation", service = ThingHandlerFactory.class)
public class YandexStationHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_STATION, THING_TYPE_BRIDGE);
    private final YandexApiFactory apiFactory;

    private static final Map<ThingUID, @NonNull YandexStationHandler> handlerMap = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Instantiates a new Yandex station handler factory.
     *
     * @param apiFactory the api factory
     */
    @Activate
    public YandexStationHandlerFactory(@Reference YandexApiFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_STATION.equals(thingTypeUID)) {
            try {
                return new YandexStationHandler(thing, apiFactory);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            try {
                return new YandexStationBridge((Bridge) thing, apiFactory);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    @Override
    protected @Nullable ThingType getThingTypeByUID(ThingTypeUID thingTypeUID) {
        return super.getThingTypeByUID(thingTypeUID);
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        super.removeHandler(thingHandler);
    }

    @Override
    public void unregisterHandler(Thing thing) {
        ThingUID uid = thing.getUID();
        if (!handlerMap.isEmpty()) {
            handlerMap.remove(uid);
        }
        super.unregisterHandler(thing);
    }

    @Override
    public ThingHandler registerHandler(Thing thing) {
        ThingHandler handler = super.registerHandler(thing);
        if (handler instanceof YandexStationHandler) {
            ThingUID uid = handler.getThing().getUID();
            handlerMap.putIfAbsent(uid, (YandexStationHandler) handler);
        }
        return handler;
    }

    /**
     * Gets thing handler by thing uid.
     *
     * @param uid the uid
     * @return the thing handler by thing uid
     */
    public static YandexStationHandler getThingHandlerByThingUID(ThingUID uid) {
        if (!handlerMap.isEmpty() && handlerMap.containsKey(uid)) {
            YandexStationHandler handler = handlerMap.get(uid);
            if (handler != null) {
                return handler;
            } else {
                throw new RuntimeException("YandexStationHandler is null");
            }
        } else {
            throw new RuntimeException(String.format("YandexStationThing with uid '%s' not found", uid));
        }
    }
}
