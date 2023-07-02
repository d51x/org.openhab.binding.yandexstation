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

package org.openhab.binding.yandexstation.internal.actions.types;

import org.openhab.binding.yandexstation.internal.YandexStationHandlerFactory;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.Input;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.FilterCriteria;

import java.util.ArrayList;
import java.util.List;

public class SayTextActionType extends ActionType {
    public static final String UID = "yandexstation.sayText";
    public static final String CONFIG_PARAM_NAME_TEXT = "sayText";
    public static final String CONFIG_PARAM_NAME_STATION = "station";
    public static final String CONFIG_PARAM_NAME_WHISPER = "whisper";
    public static final String CONFIG_TEXT = "Say Text";
    public static final String CONFIG_TEXT_DESCRIPTION = "Send text to Yandex Station for speak";
    public static final String CONFIG_STATION = "Select Station";
    public static final String CONFIG_STATION_DESCRIPTION = "Select Station";

    public static final String CONFIG_WHISPER = "Whisper";
    public static final String CONFIG_WHISPER_DESCRIPTION = "Say in a whisper";

    public static ActionType initialize() {
        // это описание конфигурационных параметров после открытия окна Add Action
        final ConfigDescriptionParameter textParam = ConfigDescriptionParameterBuilder.create(CONFIG_PARAM_NAME_TEXT,
                        ConfigDescriptionParameter.Type.TEXT)
                .withRequired(true).withReadOnly(false).withMultiple(false).withLabel(CONFIG_TEXT)
                .withDescription(CONFIG_TEXT_DESCRIPTION).build();

        //FilterCriteria filterCriteria = new FilterCriteria("id", "yandexstation:station");
        FilterCriteria filterCriteria = new FilterCriteria("bindingId", "yandexstation");
        final ConfigDescriptionParameter stationParam = ConfigDescriptionParameterBuilder.create(CONFIG_PARAM_NAME_STATION,
                        ConfigDescriptionParameter.Type.TEXT)
                .withRequired(true).withReadOnly(false).withMultiple(false).withLabel(CONFIG_STATION)
                .withFilterCriteria(List.of(filterCriteria))
                .withContext("thing")
                .withDescription(CONFIG_STATION_DESCRIPTION).build();

        final ConfigDescriptionParameter whisperParam = ConfigDescriptionParameterBuilder.create(CONFIG_PARAM_NAME_WHISPER,
                        ConfigDescriptionParameter.Type.BOOLEAN)
                .withRequired(false).withReadOnly(false).withMultiple(false).withLabel(CONFIG_WHISPER)
                .withDescription(CONFIG_WHISPER_DESCRIPTION).build();

        List<ConfigDescriptionParameter> config = new ArrayList<ConfigDescriptionParameter>();
        config.add(textParam);
        config.add(stationParam);
        config.add(whisperParam);



        Input textInput = new Input(CONFIG_PARAM_NAME_TEXT, String.class.getName(), CONFIG_TEXT, CONFIG_TEXT_DESCRIPTION,
                null, true, null, null);
        Input stationInput = new Input(CONFIG_PARAM_NAME_STATION, YandexStationHandlerFactory.class.getName(),
                CONFIG_STATION, CONFIG_STATION_DESCRIPTION, null, true, null, null);
        Input whisperInput = new Input(CONFIG_PARAM_NAME_WHISPER, Boolean.class.getName(),
                CONFIG_WHISPER, CONFIG_WHISPER_DESCRIPTION, null, false, null, "false");

        List<Input> input = new ArrayList<>();
        input.add(textInput);
        input.add(stationInput);
        input.add(whisperInput);

        return new SayTextActionType(config, input);
    }

    public SayTextActionType(List<ConfigDescriptionParameter> config, List<Input> input) {
        super(UID, config, CONFIG_TEXT, CONFIG_TEXT_DESCRIPTION, null,
                Visibility.VISIBLE, input, null);
        // отображается в окне выбора типов экшенов
    }
}
