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
package org.openhab.binding.yandexstation.internal.actions.types;

import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.BINDING_ID;
import static org.openhab.binding.yandexstation.internal.YandexStationBindingConstants.THING_TYPE_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openhab.binding.yandexstation.internal.YandexStationHandlerFactory;
import org.openhab.binding.yandexstation.internal.YandexStationTranslationProvider;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.Input;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.FilterCriteria;
import org.openhab.core.config.core.ParameterOption;

/**
 * The type Say text action type.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class SayTextActionType extends ActionType {
    /**
     * The constant UID.
     */
    public static final String UID = "yandexstation.sayText";
    /**
     * The constant CONFIG_PARAM_NAME_TEXT.
     */
    public static final String CONFIG_PARAM_NAME_TEXT = "sayText";
    /**
     * The constant CONFIG_PARAM_NAME_STATION.
     */
    public static final String CONFIG_PARAM_NAME_STATION = "station";
    /**
     * The constant CONFIG_PARAM_NAME_WHISPER.
     */
    public static final String CONFIG_PARAM_NAME_WHISPER = "whisper";
    /**
     * The constant CONFIG_PARAM_NAME_VOICE.
     */
    public static final String CONFIG_PARAM_NAME_VOICE = "voice";
    /**
     * The constant CONFIG_PARAM_NAME_PREVENT_LISTENING.
     */
    public static final String CONFIG_PARAM_NAME_PREVENT_LISTENING = "prevent_listening";
    /**
     * The constant CONFIG_TEXT.
     */
    public static final String CONFIG_TEXT = "Say Text";
    /**
     * The constant CONFIG_TEXT_DESCRIPTION.
     */
    public static final String CONFIG_TEXT_DESCRIPTION = "Send text to Yandex Station for speak";
    /**
     * The constant CONFIG_STATION.
     */
    public static final String CONFIG_STATION = "Select Station";
    /**
     * The constant CONFIG_STATION_DESCRIPTION.
     */
    public static final String CONFIG_STATION_DESCRIPTION = "Select Station";

    /**
     * The constant CONFIG_WHISPER.
     */
    public static final String CONFIG_WHISPER = "Whisper";
    /**
     * The constant CONFIG_WHISPER_DESCRIPTION.
     */
    public static final String CONFIG_WHISPER_DESCRIPTION = "Say in a whisper, works only for default voice";

    /**
     * The constant CONFIG_VOICE.
     */
    public static final String CONFIG_VOICE = "Voice";
    /**
     * The constant CONFIG_VOICE_DESCRIPTION.
     */
    public static final String CONFIG_VOICE_DESCRIPTION = "Change speaking voice";
    /**
     * The constant CONFIG_PREVENT_LISTENING.
     */
    public static final String CONFIG_PREVENT_LISTENING = "Prevent Listening";
    /**
     * The constant CONFIG_PREVENT_LISTENING_DESCRIPTION.
     */
    public static final String CONFIG_PREVENT_LISTENING_DESCRIPTION = "Don't wait for an answer";

    /**
     * Initialize action type.
     *
     * @param i18nProvider the 18 n provider
     * @return the action type
     */
    public static ActionType initialize(YandexStationTranslationProvider i18nProvider) {

        // это описание конфигурационных параметров после открытия окна Add Action
        String label, description;

        label = i18nProvider.getText("action.SayLabel", CONFIG_TEXT);
        description = i18nProvider.getText("action.SayDescription", CONFIG_TEXT_DESCRIPTION);

        final ConfigDescriptionParameter textParam = ConfigDescriptionParameterBuilder
                .create(CONFIG_PARAM_NAME_TEXT, ConfigDescriptionParameter.Type.TEXT).withRequired(true)
                .withReadOnly(false).withMultiple(false).withLabel(label).withDescription(description).build();

        Input textInput = new Input(CONFIG_PARAM_NAME_TEXT, String.class.getName(), label, description, null, true,
                null, null);

        List<FilterCriteria> filter = new ArrayList<>();
        FilterCriteria criteria1 = new FilterCriteria("bindingId", BINDING_ID);
        FilterCriteria criteria2 = new FilterCriteria("thingTypeId", THING_TYPE_ID);
        filter.add(criteria1);
        filter.add(criteria2);

        label = i18nProvider.getText("action.select_station.label", CONFIG_STATION);
        description = i18nProvider.getText("action.select_station.description", CONFIG_STATION_DESCRIPTION);

        final ConfigDescriptionParameter stationParam = ConfigDescriptionParameterBuilder
                .create(CONFIG_PARAM_NAME_STATION, ConfigDescriptionParameter.Type.TEXT).withRequired(true)
                .withReadOnly(false).withMultiple(false).withLabel(label)
                // .withFilterCriteria(filter)
                .withContext("thing")
                // .withOptions(getStations())
                // .withLimitToOptions(true)
                .withDescription(description).build();

        Input stationInput = new Input(CONFIG_PARAM_NAME_STATION, YandexStationHandlerFactory.class.getName(), label,
                description, null, true, null, null);

        label = i18nProvider.getText("action.SayTextWhisperLabel", CONFIG_WHISPER);
        description = i18nProvider.getText("action.SayTextWhisperDescription", CONFIG_WHISPER_DESCRIPTION);

        final ConfigDescriptionParameter whisperParam = ConfigDescriptionParameterBuilder
                .create(CONFIG_PARAM_NAME_WHISPER, ConfigDescriptionParameter.Type.BOOLEAN).withRequired(false)
                .withReadOnly(false).withMultiple(false).withLabel(label).withDescription(description).build();

        Input whisperInput = new Input(CONFIG_PARAM_NAME_WHISPER, Boolean.class.getName(), label, description, null,
                false, null, "false");

        label = i18nProvider.getText("action.SayTextVoiceLabel", CONFIG_VOICE);
        description = i18nProvider.getText("action.SayTextVoiceDescription", CONFIG_VOICE_DESCRIPTION);

        final ConfigDescriptionParameter voiceParam = ConfigDescriptionParameterBuilder
                .create(CONFIG_PARAM_NAME_VOICE, ConfigDescriptionParameter.Type.TEXT).withRequired(false)
                .withReadOnly(false).withMultiple(false).withLabel(label).withOptions(getAvailableVoices())
                .withDescription(description).build();

        Input voiceInput = new Input(CONFIG_PARAM_NAME_VOICE, String.class.getName(), label, description, null, false,
                null, null);

        label = i18nProvider.getText("action.preventListening.label", CONFIG_PREVENT_LISTENING);
        description = i18nProvider.getText("action.preventListening.description", CONFIG_PREVENT_LISTENING_DESCRIPTION);

        final ConfigDescriptionParameter preventListeningParam = ConfigDescriptionParameterBuilder
                .create(CONFIG_PARAM_NAME_PREVENT_LISTENING, ConfigDescriptionParameter.Type.BOOLEAN)
                .withRequired(false).withReadOnly(false).withMultiple(false).withLabel(label)
                .withDescription(description).build();

        Input preventListeningInput = new Input(CONFIG_PARAM_NAME_PREVENT_LISTENING, Boolean.class.getName(), label,
                description, null, false, null, "false");

        List<ConfigDescriptionParameter> config = new ArrayList<ConfigDescriptionParameter>();
        config.add(textParam);
        config.add(stationParam);
        config.add(whisperParam);
        config.add(voiceParam);
        config.add(preventListeningParam);

        List<Input> input = new ArrayList<>();
        input.add(textInput);
        input.add(stationInput);
        input.add(whisperInput);
        input.add(voiceInput);
        input.add(preventListeningInput);

        label = i18nProvider.getText("action.SayLabel", CONFIG_TEXT);
        description = i18nProvider.getText("action.SayDescription", CONFIG_TEXT_DESCRIPTION);

        return new SayTextActionType(config, input, label, description);
    }

    /**
     * Instantiates a new Say text action type.
     *
     * @param config the config
     * @param input the input
     * @param label the label
     * @param description the description
     */
    public SayTextActionType(List<ConfigDescriptionParameter> config, List<Input> input, String label,
            String description) {
        super(UID, config, label, description, null, Visibility.VISIBLE, input, null);
        // отображается в окне выбора типов экшенов
    }

    private static List<ParameterOption> getAvailableVoices() {
        return new ArrayList<>(Arrays.stream(org.openhab.binding.yandexstation.internal.actions.YandexVoices.values())
                .map(v -> new ParameterOption(v.getVoice(), v.getLabel())).collect(Collectors.toList()));
    }
}
