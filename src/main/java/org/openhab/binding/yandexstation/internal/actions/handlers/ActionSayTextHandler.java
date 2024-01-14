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
package org.openhab.binding.yandexstation.internal.actions.handlers;

import java.util.Map;

import org.openhab.binding.yandexstation.internal.YandexStationHandler;
import org.openhab.binding.yandexstation.internal.YandexStationHandlerFactory;
import org.openhab.binding.yandexstation.internal.actions.types.SayTextActionType;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.handler.BaseActionModuleHandler;
import org.openhab.core.thing.ThingUID;

/**
 * The type Action say text handler.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class ActionSayTextHandler extends BaseActionModuleHandler {

    @Override
    public void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
    }

    /**
     * Instantiates a new Action say text handler.
     *
     * @param module the module
     */
    public ActionSayTextHandler(final Action module) {
        super(module);
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        String text = (String) module.getConfiguration().get(SayTextActionType.CONFIG_PARAM_NAME_TEXT);
        String thing = (String) module.getConfiguration().get(SayTextActionType.CONFIG_PARAM_NAME_STATION);
        Boolean whisper = (Boolean) module.getConfiguration().get(SayTextActionType.CONFIG_PARAM_NAME_WHISPER);
        String voice = (String) module.getConfiguration().get(SayTextActionType.CONFIG_PARAM_NAME_VOICE);
        Boolean preventListening = (Boolean) module.getConfiguration()
                .get(SayTextActionType.CONFIG_PARAM_NAME_PREVENT_LISTENING);

        YandexStationHandler handler = YandexStationHandlerFactory.getThingHandlerByThingUID(new ThingUID(thing));
        // нужен способ как-то получить handler нужного Thing по thingUID
        if (whisper != null && whisper) {
            text = "<speaker is_whisper='true'>" + text;
        } else if (voice != null && !voice.isEmpty()) {
            text = "<speaker voice='" + voice + "'>" + text;
        }
        handler.sendTtsCommand(text);
        if (preventListening != null && preventListening) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            handler.sendStopListening();
        }
        return null;
    }
}
