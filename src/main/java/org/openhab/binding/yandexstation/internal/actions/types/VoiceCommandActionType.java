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
package org.openhab.binding.yandexstation.internal.actions.types;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.yandexstation.internal.YandexStationTranslationProvider;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.Input;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.FilterCriteria;
import org.openhab.core.thing.type.ThingType;

/**
 * The type Voice command action type.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class VoiceCommandActionType extends ActionType {
    /**
     * The constant UID.
     */
    public static final String UID = "yandexstation.voiceCommand";
    /**
     * The constant CONFIG_PARAM_NAME_TEXT.
     */
    public static final String CONFIG_PARAM_NAME_TEXT = "voiceCommand";
    /**
     * The constant CONFIG_PARAM_NAME_STATION.
     */
    public static final String CONFIG_PARAM_NAME_STATION = "station";
    /**
     * The constant CONFIG_TEXT.
     */
    public static final String CONFIG_TEXT = "Voice Command";
    /**
     * The constant CONFIG_TEXT_DESCR.
     */
    public static final String CONFIG_TEXT_DESCR = "Send text to Yandex Station for voice command";
    /**
     * The constant CONFIG_STATION.
     */
    public static final String CONFIG_STATION = "Select Station";
    /**
     * The constant CONFIG_STATION_DESCR.
     */
    public static final String CONFIG_STATION_DESCR = "Select Station";

    /**
     * Initialize action type.
     *
     * @param i18nProvider the 18 n provider
     * @return the action type
     */
    public static ActionType initialize(YandexStationTranslationProvider i18nProvider) {
        // это описание конфигурационных параметров после открытия окна Add Action

        String label = i18nProvider.getText("action.VoiceCommandLabel", CONFIG_TEXT);
        String description = i18nProvider.getText("action.VoiceCommandDescription", CONFIG_TEXT_DESCR);

        final ConfigDescriptionParameter textParam = ConfigDescriptionParameterBuilder
                .create(CONFIG_PARAM_NAME_TEXT, ConfigDescriptionParameter.Type.TEXT).withRequired(true)
                .withReadOnly(false).withMultiple(false).withLabel(label).withDescription(description).build();

        Input textInput = new Input(CONFIG_PARAM_NAME_TEXT, String.class.getName(), label, description, null, true,
                null, null);

        label = i18nProvider.getText("action.select_station.label", CONFIG_STATION);
        description = i18nProvider.getText("action.select_station.description", CONFIG_STATION_DESCR);

        // thingTypeUID
        FilterCriteria filterType = new FilterCriteria("thingTypeUID", "yandexstation:station");
        List<FilterCriteria> filters = new ArrayList<>();
        filters.add(filterType);
        final ConfigDescriptionParameter stationParam = ConfigDescriptionParameterBuilder
                .create(CONFIG_PARAM_NAME_STATION, ConfigDescriptionParameter.Type.TEXT).withRequired(true)
                .withReadOnly(false).withMultiple(false).withLabel(label).withFilterCriteria(filters)
                .withContext("thing").withDescription(description).build();

        Input stationInput = new Input(CONFIG_PARAM_NAME_STATION, ThingType.class.getName(), label, description, null,
                true, null, null);

        List<ConfigDescriptionParameter> config = new ArrayList<ConfigDescriptionParameter>();
        config.add(textParam);
        config.add(stationParam);

        List<Input> input = new ArrayList<>();
        input.add(textInput);
        input.add(stationInput);

        label = i18nProvider.getText("action.VoiceCommandLabel", CONFIG_TEXT);
        description = i18nProvider.getText("action.VoiceCommandDescription", CONFIG_TEXT_DESCR);

        return new VoiceCommandActionType(config, input, label, description);
    }

    /**
     * Instantiates a new Voice command action type.
     *
     * @param config the config
     * @param input the input
     * @param label the label
     * @param description the description
     */
    public VoiceCommandActionType(List<ConfigDescriptionParameter> config, List<Input> input, String label,
            String description) {
        super(UID, config, label, description, null, Visibility.VISIBLE, input, null);
        // отображается в окне выбора типов экшенов
    }
}
