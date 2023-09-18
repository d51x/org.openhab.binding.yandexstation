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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIScenarioResponse;
import org.openhab.core.thing.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link YandexStationScenarios} is describing api common success response.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */

@NonNullByDefault
public class YandexStationScenarios {
    private final Logger logger = LoggerFactory.getLogger(YandexStationScenarios.class);
    private int id;
    APIScenarioResponse.@Nullable Scenarios scn;
    @Nullable
    Channel channel = null;
    String jsonScenario = "";
    public static String SEPARATOR_CHARS = "---";

    public void addScenario(APIScenarioResponse.Scenarios scn, Channel channel, String x) {
        this.scn = scn;
        this.channel = channel;
    }

    public APIScenarioResponse.@Nullable Scenarios getScn() {
        return scn;
    }

    public @Nullable Channel getChannel() {
        return channel;
    }

    public String createScenario(Channel channel, String x) {
        this.channel = channel;
        Gson gson = new Gson();
        @Nullable
        ScenarioJson scenarioJson = new ScenarioJson();
        Triggers trigger = new Triggers();
        scenarioJson.name = SEPARATOR_CHARS + " " + channel.getLabel();
        scenarioJson.icon = "home";
        trigger.type = "scenario.trigger.voice";
        trigger.value = Objects.requireNonNull(channel.getLabel());
        scenarioJson.triggers = new Triggers[] { trigger };
        Steps steps = new Steps();
        steps.type = "scenarios.steps.actions";
        Parameters parameters = new Parameters();
        parameters.launch_devices = new String[] {};

        RequestedSpeakerCapabilities requestedSpeakerCapabilities = new RequestedSpeakerCapabilities();
        Pparameters pparameters = new Pparameters();
        pparameters.instance = "text_action";
        Sstate sstate = new Sstate();
        sstate.instance = "text_action";
        if (channel.getConfiguration().get("answer") != null) {
            sstate.value = channel.getConfiguration().get("answer").toString() + SEPARATOR_CHARS + x;
        } else {
            sstate.value = "Сделай громкость" + SEPARATOR_CHARS + x;
        }
        requestedSpeakerCapabilities.type = "devices.capabilities.quasar.server_action";
        requestedSpeakerCapabilities.parameters = pparameters;
        requestedSpeakerCapabilities.sstate = sstate;
        parameters.requestedSpeakerCapabilities = new RequestedSpeakerCapabilities[] { requestedSpeakerCapabilities };
        steps.parameters = parameters;
        scenarioJson.steps = new Steps[] { steps };
        jsonScenario = gson.toJson(scenarioJson);
        return jsonScenario;
    }

    public String updateScenario(String x) {
        Gson gson = new Gson();
        @Nullable
        ScenarioJson scenarioJson = new ScenarioJson();
        Triggers trigger = new Triggers();
        scenarioJson.name = SEPARATOR_CHARS + " " + channel.getLabel();
        scenarioJson.icon = "home";
        trigger.type = "scenario.trigger.voice";
        trigger.value = Objects.requireNonNull(channel.getLabel());
        scenarioJson.triggers = new Triggers[] { trigger };
        Steps steps = new Steps();
        steps.type = "scenarios.steps.actions";
        Parameters parameters = new Parameters();
        parameters.launch_devices = new String[] {};

        RequestedSpeakerCapabilities requestedSpeakerCapabilities = new RequestedSpeakerCapabilities();
        Pparameters pparameters = new Pparameters();
        pparameters.instance = "text_action";
        Sstate sstate = new Sstate();
        sstate.instance = "text_action";
        if (channel.getConfiguration().get("answer") != null) {
            sstate.value = channel.getConfiguration().get("answer").toString() + SEPARATOR_CHARS + x;
        } else {
            sstate.value = "Сделай громкость" + SEPARATOR_CHARS + x;
        }
        requestedSpeakerCapabilities.type = "devices.capabilities.quasar.server_action";
        requestedSpeakerCapabilities.parameters = pparameters;
        requestedSpeakerCapabilities.sstate = sstate;
        parameters.requestedSpeakerCapabilities = new RequestedSpeakerCapabilities[] { requestedSpeakerCapabilities };
        steps.parameters = parameters;
        scenarioJson.steps = new Steps[] { steps };
        jsonScenario = gson.toJson(scenarioJson);
        return jsonScenario;
    }

    public class Triggers {
        @Nullable
        public String type;
        @Nullable
        public String value;
    }

    public class Steps {
        @Nullable
        String type;
        @Nullable
        Parameters parameters = new Parameters();
    }

    class Parameters {
        @Nullable
        String launch_devices[] = new String[0];
        @Nullable
        @SerializedName("requested_speaker_capabilities")
        RequestedSpeakerCapabilities[] requestedSpeakerCapabilities = new RequestedSpeakerCapabilities[0];
    }

    public class RequestedSpeakerCapabilities {
        @Nullable
        Pparameters parameters;
        @Nullable
        String type;
        @Nullable
        @SerializedName("state")
        Sstate sstate;
    }

    public class Pparameters {
        @Nullable
        String instance;
    }

    public class Sstate {
        @Nullable
        String instance;
        @Nullable
        String value;
    }

    public class ScenarioJson {
        @Nullable
        String name;
        @Nullable
        String icon;
        public Triggers triggers[] = new Triggers[0];
        @Nullable
        Steps[] steps = new Steps[0];;
    }
}
