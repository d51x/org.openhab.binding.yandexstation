/*
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information.
 *
 * This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.yandexstation.internal.actions.handlers;

import org.openhab.binding.yandexstation.internal.YandexStationHandler;
import org.openhab.binding.yandexstation.internal.YandexStationHandlerFactory;
import org.openhab.binding.yandexstation.internal.actions.types.SayTextActionType;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.handler.BaseActionModuleHandler;
import org.openhab.core.thing.ThingUID;

import java.util.Map;


public class ActionSayTextHandler extends BaseActionModuleHandler {

    @Override
    public void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
    }

    public ActionSayTextHandler(final Action module) {
        super(module);
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        String text = (String) module.getConfiguration().get(SayTextActionType.CONFIG_PARAM_NAME_TEXT);
        String thing = (String) module.getConfiguration().get(SayTextActionType.CONFIG_PARAM_NAME_STATION);
        Boolean whisper = (Boolean) module.getConfiguration().get(SayTextActionType.CONFIG_PARAM_NAME_WHISPER);
        String voice = (String) module.getConfiguration().get(SayTextActionType.CONFIG_PARAM_NAME_VOICE);

        YandexStationHandler handler = YandexStationHandlerFactory.getThingHandlerByThingUID(new ThingUID(thing));
        // нужен способ как-то получить handler нужного Thing по thingUID
        if (whisper) {
            text = "<speaker is_whisper='true'>" + text;
        } else if (voice != null && !voice.isEmpty()) {
            text = "<speaker voice='" + voice + "'>" + text;
        }
        handler.sendTtsCommand(text);
        return null;
    }
}
