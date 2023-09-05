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
package org.openhab.binding.yandexstation.internal.yandexapi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIExtendedResponse;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIScenarioResponse;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link YandexApiScenarios} is describing implementaion of api interface.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexApiScenarios implements YandexApi {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final HttpClient httpClient;

    public YandexApiScenarios(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void update() throws ApiException {
    }

    @Override
    public void initialize() throws ApiException {
    }

    @Override
    public APIExtendedResponse sendGetRequest(String path, @Nullable String params, String token) throws ApiException {
        return new APIExtendedResponse();
    }

    @Override
    public APIExtendedResponse sendGetRequest(String path, String token) throws ApiException {
        return new APIExtendedResponse();
    }

    @Override
    public APIExtendedResponse sendPostRequest(String path, String data, String token) throws ApiException {
        return new APIExtendedResponse();
    }

    @Override
    public APIExtendedResponse sendPostRequest(String path, Fields fields, String token) throws ApiException {
        return new APIExtendedResponse();
    }

    public @Nullable String readCookieSession() {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "sessionCookie");
        if (!file.exists()) {
            return null;
        } else {
            List<String> lines = null;
            try {
                lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
            return lines == null || lines.isEmpty() ? null : lines.get(0);
        }
    }

    public List<APIScenarioResponse> getScenarios() {
        List<APIScenarioResponse> scenarios = new ArrayList<>();
        try {
            APIExtendedResponse response = sendGetRequest("https://iot.quasar.yandex.ru/m/user/scenarios", null,
                    Objects.requireNonNull(readCookieSession()));
            Gson gson = new Gson();
            APIScenarioResponse scenarioResponse = gson.fromJson(response.response, APIScenarioResponse.class);
            logger.debug("Scenarios json is: {}", response.response);
        } catch (ApiException e) {
        }
        return scenarios;
    }
}
