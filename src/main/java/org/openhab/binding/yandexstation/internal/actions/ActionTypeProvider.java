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

package org.openhab.binding.yandexstation.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yandexstation.internal.actions.types.SayTextActionType;
import org.openhab.binding.yandexstation.internal.actions.types.VoiceCommandActionType;
import org.openhab.core.automation.type.ModuleType;
import org.openhab.core.automation.type.ModuleTypeProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Component;

import java.util.*;

@NonNullByDefault
@Component(service={ModuleTypeProvider.class})
public class ActionTypeProvider implements ModuleTypeProvider {

    private Map<String, ModuleType> providedModuleTypes;

    public ActionTypeProvider() {
        providedModuleTypes = new HashMap<>();
        providedModuleTypes.put(SayTextActionType.UID, SayTextActionType.initialize());
        providedModuleTypes.put(VoiceCommandActionType.UID, VoiceCommandActionType.initialize());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> @Nullable T getModuleType(String UID, @Nullable Locale locale) {
        return (T) providedModuleTypes.get(UID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ModuleType> Collection<T> getModuleTypes(@Nullable Locale locale) {
        return (Collection<T>) providedModuleTypes.values();
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> providerChangeListener) {
        // does nothing because this provider does not change
    }

    @Override
    public Collection<ModuleType> getAll() {
        return Collections.unmodifiableCollection(providedModuleTypes.values());
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> providerChangeListener) {
        // does nothing because this provider does not change
    }
}
