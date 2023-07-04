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
import org.openhab.core.config.core.ParameterOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.BINDING_ID;
import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.THING_TYPE_ID;


public class SayTextActionType extends ActionType {
    public static final String UID = "yandexstation.sayText";
    public static final String CONFIG_PARAM_NAME_TEXT = "sayText";
    public static final String CONFIG_PARAM_NAME_STATION = "station";
    public static final String CONFIG_PARAM_NAME_WHISPER = "whisper";
    public static final String CONFIG_PARAM_NAME_VOICE = "voice";
    public static final String CONFIG_PARAM_NAME_PREVENT_LISTENING = "prevent_listening";
    public static final String CONFIG_TEXT = "Say Text";
    public static final String CONFIG_TEXT_DESCRIPTION = "Send text to Yandex Station for speak";
    public static final String CONFIG_STATION = "Select Station";
    public static final String CONFIG_STATION_DESCRIPTION = "Select Station";

    public static final String CONFIG_WHISPER = "Whisper";
    public static final String CONFIG_WHISPER_DESCRIPTION = "Say in a whisper, works only for default voice";

    public static final String CONFIG_VOICE = "Voice";
    public static final String CONFIG_VOICE_DESCRIPTION = "Change speaking voice";
    public static final String CONFIG_PREVENT_LISTENING = "Prevent Listening";
    public static final String CONFIG_PREVENT_LISTENING_DESCRIPTION = "Don't wait for an answer";

    public static ActionType initialize() {
        // это описание конфигурационных параметров после открытия окна Add Action
        final ConfigDescriptionParameter textParam = ConfigDescriptionParameterBuilder.create(CONFIG_PARAM_NAME_TEXT,
                        ConfigDescriptionParameter.Type.TEXT)
                .withRequired(true).withReadOnly(false).withMultiple(false).withLabel(CONFIG_TEXT)
                .withDescription(CONFIG_TEXT_DESCRIPTION).build();

        List<FilterCriteria> filter = new ArrayList<>();
        FilterCriteria criteria1 = new FilterCriteria("bindingId", BINDING_ID);
        FilterCriteria criteria2 = new FilterCriteria("thingTypeId", THING_TYPE_ID);
        filter.add(criteria1);
        filter.add(criteria2);

        final ConfigDescriptionParameter stationParam = ConfigDescriptionParameterBuilder.create(CONFIG_PARAM_NAME_STATION,
                        ConfigDescriptionParameter.Type.TEXT)
                .withRequired(true).withReadOnly(false).withMultiple(false).withLabel(CONFIG_STATION)
                //.withFilterCriteria(filter)
                .withContext("thing")
                //.withOptions(getStations())
                //.withLimitToOptions(true)
                .withDescription(CONFIG_STATION_DESCRIPTION).build();

        final ConfigDescriptionParameter whisperParam = ConfigDescriptionParameterBuilder.create(CONFIG_PARAM_NAME_WHISPER,
                        ConfigDescriptionParameter.Type.BOOLEAN)
                .withRequired(false).withReadOnly(false).withMultiple(false).withLabel(CONFIG_WHISPER)
                .withDescription(CONFIG_WHISPER_DESCRIPTION).build();

        final ConfigDescriptionParameter voiceParam = ConfigDescriptionParameterBuilder.create(CONFIG_PARAM_NAME_VOICE,
                        ConfigDescriptionParameter.Type.TEXT)
                .withRequired(false).withReadOnly(false).withMultiple(false).withLabel(CONFIG_VOICE)
                .withOptions(getAvailableVoices())
                .withDescription(CONFIG_VOICE_DESCRIPTION).build();

        final ConfigDescriptionParameter preventListeningParam = ConfigDescriptionParameterBuilder.create(CONFIG_PARAM_NAME_PREVENT_LISTENING,
                        ConfigDescriptionParameter.Type.BOOLEAN)
                .withRequired(false).withReadOnly(false).withMultiple(false).withLabel(CONFIG_PREVENT_LISTENING)
                .withDescription(CONFIG_PREVENT_LISTENING_DESCRIPTION).build();

        List<ConfigDescriptionParameter> config = new ArrayList<ConfigDescriptionParameter>();
        config.add(textParam);
        config.add(stationParam);
        config.add(whisperParam);
        config.add(voiceParam);
        config.add(preventListeningParam);



        Input textInput = new Input(CONFIG_PARAM_NAME_TEXT, String.class.getName(), CONFIG_TEXT, CONFIG_TEXT_DESCRIPTION,
                null, true, null, null);
        Input stationInput = new Input(CONFIG_PARAM_NAME_STATION, YandexStationHandlerFactory.class.getName(),
                CONFIG_STATION, CONFIG_STATION_DESCRIPTION, null, true, null, null);
        Input whisperInput = new Input(CONFIG_PARAM_NAME_WHISPER, Boolean.class.getName(),
                CONFIG_WHISPER, CONFIG_WHISPER_DESCRIPTION, null, false, null, "false");
        Input voiceInput = new Input(CONFIG_PARAM_NAME_VOICE, String.class.getName(),
                CONFIG_VOICE, CONFIG_VOICE_DESCRIPTION, null, false, null, null);
        Input preventListeningInput = new Input(CONFIG_PARAM_NAME_PREVENT_LISTENING, Boolean.class.getName(),
                CONFIG_PREVENT_LISTENING, CONFIG_PREVENT_LISTENING_DESCRIPTION, null, false, null, "false");

        List<Input> input = new ArrayList<>();
        input.add(textInput);
        input.add(stationInput);
        input.add(whisperInput);
        input.add(voiceInput);
        input.add(preventListeningInput);

        return new SayTextActionType(config, input);
    }

    public SayTextActionType(List<ConfigDescriptionParameter> config, List<Input> input) {
        super(UID, config, CONFIG_TEXT, CONFIG_TEXT_DESCRIPTION, null,
                Visibility.VISIBLE, input, null);
        // отображается в окне выбора типов экшенов
    }

    private static List<ParameterOption> getAvailableVoices() {
        return new ArrayList<>(Arrays.stream(org.openhab.voice.yandexstation.internal.YandexVoices.values())
                .map(v -> new ParameterOption(v.getVoice(), v.getLabel()))
                .collect(Collectors.toList()));
    }

}
