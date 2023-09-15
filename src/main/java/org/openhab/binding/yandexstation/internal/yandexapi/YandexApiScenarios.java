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
import java.net.CookieStore;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIExtendedResponse;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void update() {
    }

    @Override
    public void initialize() throws ApiException {
    }

    @Override
    public APIExtendedResponse sendGetRequest(String path, @Nullable String params, String token) throws ApiException {
        String cookie = readCookie();
        APIExtendedResponse result = new APIExtendedResponse();
        Request request = null;
        httpClient.setConnectTimeout(60 * 1000);
        if (params != null) {
            request = httpClient.newRequest(path + params);
        } else {
            request = httpClient.newRequest(path);
        }
        setHeaders(request, cookie);
        request.method(HttpMethod.GET);
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                result.response = contentResponse.getContentAsString();
                CookieStore cs = httpClient.getCookieStore();
                List<HttpCookie> cck = cs.getCookies();
                return result;
            } else {
                errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("ERROR {}", e.getMessage());
        }
        throw new ApiException(errorReason);
    }

    @Override
    public APIExtendedResponse sendGetRequest(String path, String token) {
        return new APIExtendedResponse();
    }

    @Override
    public APIExtendedResponse sendPostRequest(String path, String data, String token) {
        return new APIExtendedResponse();
    }

    @Override
    public APIExtendedResponse sendPostRequest(String path, Fields fields, String token) {
        return new APIExtendedResponse();
    }

    private void setHeaders(Request request, @Nullable String cookie) {
        String YANDEX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
        request.timeout(60, TimeUnit.SECONDS);
        request.header(HttpHeader.USER_AGENT, null);
        request.header(HttpHeader.USER_AGENT, YANDEX_USER_AGENT);
        request.header(HttpHeader.CONNECTION, "keep-alive");
        request.header(HttpHeader.ACCEPT, "*/*");
        request.header(HttpHeader.ACCEPT_ENCODING, null);
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate, br");
        if (cookie != null) {
            request.header(HttpHeader.COOKIE, cookie);
        }
        request.followRedirects(true);
    }

    public @Nullable String readCookie() {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "passportCookie");
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
}
