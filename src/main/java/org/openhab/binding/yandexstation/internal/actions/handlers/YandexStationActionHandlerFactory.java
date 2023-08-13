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
package org.openhab.binding.yandexstation.internal.actions.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yandexstation.internal.actions.types.SayTextActionType;
import org.openhab.binding.yandexstation.internal.actions.types.VoiceCommandActionType;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Yandex station action handler factory.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
@Component(service = { ModuleHandlerFactory.class })
public class YandexStationActionHandlerFactory extends BaseModuleHandlerFactory {
    /**
     * The constant MODULE_HANDLER_FACTORY_NAME.
     */
    public static final String MODULE_HANDLER_FACTORY_NAME = "[YandexStationActionHandlerFactory]";

    static {
        List<String> types = new ArrayList<String>();
        types.add(SayTextActionType.UID);
        types.add(VoiceCommandActionType.UID);
        TYPES = Collections.unmodifiableCollection(types);
    }

    private static final Collection<String> TYPES;

    private final Logger logger = LoggerFactory.getLogger(YandexStationActionHandlerFactory.class);

    // Tell the automation engine about our handlers
    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    @Override
    protected @Nullable ModuleHandler internalCreate(Module module, String ruleUID) {
        ModuleHandler moduleHandler = null;
        if (SayTextActionType.UID.equals(module.getTypeUID())) {
            moduleHandler = new ActionSayTextHandler((Action) module);
        } else if (VoiceCommandActionType.UID.equals(module.getTypeUID())) {
            moduleHandler = new ActionVoiceCommandHandler((Action) module);
        } else {
            logger.warn(MODULE_HANDLER_FACTORY_NAME + "Not supported moduleHandler: {}", module.getTypeUID());
        }
        return moduleHandler;
    }
}
