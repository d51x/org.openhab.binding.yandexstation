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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.yandexstation.internal.yandexapi.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexApiGetTokens} is describing implementaion of api interface.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexApiGetTokens implements YandexApi {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final HttpClient httpClient;
    public static final String YANDEX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
    public static final String API_URL = "https://passport.yandex.ru/";

    public YandexApiGetTokens(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void update() throws ApiException {
    }

    @Override
    public void initialize() throws ApiException {
    }

    @Override
    public ApiResponse sendGetRequest(String path, @NonNull String params, String token) throws ApiException {
        ApiResponse result = new ApiResponse();
        httpClient.setConnectTimeout(60 * 1000);
        Request request = httpClient.newRequest(path + params);
        setHeaders(request, null);
        request.method(HttpMethod.GET);
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                result.response = contentResponse.getContentAsString();
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
    public ApiResponse sendGetRequest(String path, String token) throws ApiException {
        ApiResponse result = new ApiResponse();
        httpClient.setConnectTimeout(60 * 1000);
        return new ApiResponse();
    }

    @Override
    public ApiResponse sendPostRequest(String path, String data, String token) throws ApiException {

        return new ApiResponse();
    }

    @Override
    public ApiResponse sendPostRequest(String path, Fields fields, String token) throws ApiException {
        return new ApiResponse();
    }

    public String getToken(String username, String password) throws ApiException {
        String csrf_token = "";
        ApiResponse csrf_token_request = sendGetRequest(API_URL + "am?", "app_platform=android", "");
        final Document doc = Jsoup.parse(csrf_token_request.response);
        for (Element current : doc.body().getElementsByTag("input")) {
            if (current.attr("name").equals("csrf_token")) {
                csrf_token = current.attr("value");
            }
        }
        ApiResponse track_id_request = sendPostRequest(API_URL + "https://passport.yandex.ru/registration-validations/auth/multi_step/start", "csrf_token=" + csrf_token + "login="+username, "");
        logger.debug("csrf_token: {}", csrf_token);
        return "csrf_token";
    }

    private void setHeaders(Request request, @Nullable String token) {
        request.timeout(60, TimeUnit.SECONDS);
        request.header(HttpHeader.USER_AGENT, null);
        request.header(HttpHeader.USER_AGENT, YANDEX_USER_AGENT);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.header(HttpHeader.CONNECTION, "keep-alive");
        request.header(HttpHeader.ACCEPT, "*/*");
        request.header(HttpHeader.ACCEPT_ENCODING, null);
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate, br");

        if (token != null) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + token);
        }
        request.followRedirects(true);
    }
}
