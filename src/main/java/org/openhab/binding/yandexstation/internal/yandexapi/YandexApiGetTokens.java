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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APICloudDevicesResponse;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIExtendedResponse;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    public APIExtendedResponse sendGetRequest(String path, @Nullable String params, @Nullable String token)
            throws ApiException {
        String cookie = readCookie();
        APIExtendedResponse result = new APIExtendedResponse();
        Request request = null;
        httpClient.setConnectTimeout(60 * 1000);
        if (params != null) {
            request = httpClient.newRequest(path + params);
        } else {
            request = httpClient.newRequest(path);
        }
        if (token != null) {
            setHeaders(request, null, token);
        } else {
            setHeaders(request, null, cookie);
        }
        request.method(HttpMethod.GET);
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                result.response = contentResponse.getContentAsString();
                CookieStore cs = httpClient.getCookieStore();
                List<HttpCookie> cck = cs.getCookies();
                writeCookie(cck);
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
    public APIExtendedResponse sendGetRequest(String path, String token) throws ApiException {
        APIExtendedResponse result = new APIExtendedResponse();
        httpClient.setConnectTimeout(60 * 1000);
        return new APIExtendedResponse();
    }

    @Override
    public APIExtendedResponse sendPostRequest(String path, String data, String token) throws ApiException {
        String cookies = readCookie();
        String errorReason = "";
        APIExtendedResponse result = new APIExtendedResponse();
        httpClient.setConnectTimeout(60 * 1000);
        Request request = httpClient.newRequest(path);
        request.timeout(60, TimeUnit.SECONDS);
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.header("charset", "utf-8");
        request.header(HttpHeader.CONTENT_LENGTH, Integer.toString(data.length()));
        if (cookies != null) {
            request.header(HttpHeader.COOKIE, cookies);
        }
        // request.followRedirects(false);
        request.content(new StringContentProvider(data), "application/x-www-form-urlencoded");

        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                result.response = contentResponse.getContentAsString();
                CookieStore cs = httpClient.getCookieStore();
                result.cookies = cs.getCookies();
                writeCookie(result.cookies);
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
    public APIExtendedResponse sendPostRequest(String path, Fields fields, String token) throws ApiException {
        return new APIExtendedResponse();
    }

    private APIExtendedResponse sendPostRequestForToken(String path, String data) throws ApiException {
        String errorReason = "";
        if (readCookieSession() != null) {
            String cookies = readCookie();
            APIExtendedResponse result = new APIExtendedResponse();
            httpClient.setConnectTimeout(60 * 1000);
            Request request = httpClient.newRequest(path);
            request.timeout(60, TimeUnit.SECONDS);
            request.method(HttpMethod.POST);
            request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.header("charset", "utf-8");
            request.header(HttpHeader.CONTENT_LENGTH, Integer.toString(data.length()));
            request.header("ya-client-host", "passport.yandex.ru");
            request.header("ya-client-cookie", readCookieSession());
            if (cookies != null) {
                request.header(HttpHeader.COOKIE, cookies);
            }
            request.content(new StringContentProvider(data), "application/x-www-form-urlencoded");

            try {
                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                    result.response = contentResponse.getContentAsString();
                    CookieStore cs = httpClient.getCookieStore();
                    result.cookies = cs.getCookies();
                    writeCookie(result.cookies);
                    return result;
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("ERROR {}", e.getMessage());
            }
        }

        throw new ApiException(errorReason);
    }

    public void getToken(String username, String password) throws ApiException {
        if (readCookie() != null) {
            String cookies = readCookie();
            if (cookies != null) {
                String[] cookiesList = cookies.split(",");
                for (String cookie : cookiesList) {
                    if (cookie.contains("Session_id=")) {
                        writeCookieSession(cookie);
                    }
                }
            }
        }
        if (readCookieSession() == null) {
            String csrf_token = "";
            sendGetRequest(API_URL, null, null);
            APIExtendedResponse csrfTokenRequest = sendGetRequest(API_URL + "am?", "app_platform=android", null);
            String getCsrfTokenString = csrfTokenRequest.response
                    .substring(csrfTokenRequest.response.indexOf("name=\"csrf_token\""),
                            csrfTokenRequest.response.indexOf("name=\"csrf_token\"") + csrfTokenRequest.response
                                    .substring(csrfTokenRequest.response.indexOf("name=\"csrf_token\""))
                                    .indexOf("\"/>"));
            String[] parseToken = getCsrfTokenString.replace("\"", "").split("=");
            if (Arrays.asList(parseToken).contains("csrf_token value")) {
                logger.debug("csrf_token {}", parseToken[2]);
                csrf_token = parseToken[2];
            }
            String trackId = "";
            APIExtendedResponse trackIdRequest = sendPostRequest(
                    API_URL + "registration-validations/auth/multi_step/start",
                    "csrf_token=" + csrf_token + "&login=" + username, "");
            JsonObject trackIdObj = JsonParser.parseString(trackIdRequest.response).getAsJsonObject();
            if (trackIdObj.has("status")) {
                if (trackIdObj.get("status").getAsString().equals("ok")) {
                    if (trackIdObj.has("can_authorize")) {
                        if (trackIdObj.get("can_authorize").getAsBoolean()) {
                            trackId = trackIdObj.get("track_id").getAsString();
                            logger.debug("track_id {}", trackId);
                        }
                    }

                }
            } else {
                throw new ApiException("Cannot fetch track_id");
            }
            APIExtendedResponse passwordCheck = sendPostRequest(
                    API_URL + "registration-validations/auth/multi_step/commit_password",
                    "csrf_token=" + csrf_token + "&track_id=" + trackId + "&password=" + password, "");

            String response = passwordCheck.response;
            if (JsonParser.parseString(trackIdRequest.response).getAsJsonObject().has("status")) {
                if (JsonParser.parseString(trackIdRequest.response).getAsJsonObject().get("status").getAsString()
                        .equals("ok")) {
                    passwordCheck.cookies.forEach(sessionCookie -> {
                        if (sessionCookie.getName().equals("Session_id")) {
                            writeCookieSession(sessionCookie.toString());
                        }
                    });
                } else {
                    if (JsonParser.parseString(trackIdRequest.response).getAsJsonObject().has("errors")) {
                        throw new ApiException(JsonParser.parseString(trackIdRequest.response).getAsJsonObject()
                                .get("errors").getAsJsonArray().toString());
                    } else
                        throw new ApiException("Error sending password");
                }
            } else
                throw new ApiException("Password error");
            if (readCookieSession() != null) {
                APIExtendedResponse getUserToken = sendPostRequestForToken(
                        "https://mobileproxy.passport.yandex.net/1/bundle/oauth/token_by_sessionid",
                        "client_id=c0ebe342af7d48fbbbfcf2d2eedb8f9e&client_secret=ad0a908f0aa341a182a37ecd75bc319e&track_id="
                                + trackId);
                logger.debug("Token resonse is {}", getUserToken.response);
                JsonObject token = JsonParser.parseString(getUserToken.response).getAsJsonObject();
                if (token.has("status")) {
                    if (token.get("status").getAsString().equals("ok")) {
                        writeXtoken(token.get("access_token").getAsString());
                    }
                }
                APIExtendedResponse getMusicToken = sendPostRequestForToken("https://oauth.mobile.yandex.net/1/token",
                        "client_id=23cabbbdc6cd418abb4b39c32c41195d&client_secret=53bc75238f0c4d08a118e51fe9203300&grant_type=x-token&access_token="
                                + token.get("access_token").getAsString());
                JsonObject getMusicTokenJson = JsonParser.parseString(getMusicToken.response).getAsJsonObject();
                if (getMusicTokenJson.has("access_token")) {
                    writeMusicToken(getMusicTokenJson.get("access_token").getAsString());
                }

            }
        } else {
            // APIExtendedResponse csrf = sendGetRequest("https://yandex.ru/quasar/iot", null, null);
            // String getCsrfTokenString = csrf.response.substring(csrf.response.indexOf("csrfToken2"),
            // csrf.response.indexOf("csrfToken2")
            // + csrf.response.substring(csrf.response.indexOf("csrfToken2")).indexOf(","));
            // String[] parseToken = getCsrfTokenString.split(":");
            // APIExtendedResponse checkCookie = sendGetRequest("https://quasar.yandex.ru/get_account_config", null,
            // readCookieSession());
            if (readXtoken() == null) {
                APIExtendedResponse getXToken = sendPostRequestForToken(
                        "https://mobileproxy.passport.yandex.net/1/bundle/oauth/token_by_sessionid",
                        "client_id=c0ebe342af7d48fbbbfcf2d2eedb8f9e&client_secret=ad0a908f0aa341a182a37ecd75bc319e");
                // logger.debug("Token resonse is {}", getXToken.response);
                JsonObject token = JsonParser.parseString(getXToken.response).getAsJsonObject();
                if (token.has("status")) {
                    if (token.get("status").getAsString().equals("ok")) {
                        writeXtoken(token.get("access_token").getAsString());
                    } else {
                        logger.debug("Cannot fetch Xtoken");
                    }
                }

            }

            APIExtendedResponse getMusicToken = sendPostRequestForToken("https://oauth.mobile.yandex.net/1/token",
                    "client_id=23cabbbdc6cd418abb4b39c32c41195d&client_secret=53bc75238f0c4d08a118e51fe9203300&grant_type=x-token&access_token="
                            + readXtoken());
            JsonObject getMusicTokenJson = JsonParser.parseString(getMusicToken.response).getAsJsonObject();
            if (getMusicTokenJson.has("access_token")) {
                writeMusicToken(getMusicTokenJson.get("access_token").getAsString());
            }
        }
    }

    public APICloudDevicesResponse getDevicesList() throws ApiException {
        APIExtendedResponse response = sendGetRequest("https://iot.quasar.yandex.ru/m/v3/user/devices", null,
                readCookieSession());
        APICloudDevicesResponse resp = new APICloudDevicesResponse();
        Gson gson = new Gson();
        resp = gson.fromJson(response.response, APICloudDevicesResponse.class);
        return Objects.requireNonNullElseGet(resp, APICloudDevicesResponse::new);
    }

    public String getWssUrl() throws ApiException {
        return getDevicesList().updates_url;
    }

    private void setHeaders(Request request, @Nullable String token, @Nullable String cookie) {
        request.timeout(60, TimeUnit.SECONDS);
        request.header(HttpHeader.USER_AGENT, null);
        request.header(HttpHeader.USER_AGENT, YANDEX_USER_AGENT);
        request.header(HttpHeader.CONNECTION, "keep-alive");
        request.header(HttpHeader.ACCEPT, "*/*");
        request.header(HttpHeader.ACCEPT_ENCODING, null);
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate, br");

        if (token != null) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + token);
        }
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

    private void writeCookie(List<HttpCookie> cookies) {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "passportCookie");
        boolean createOk = file.getParentFile().mkdirs();
        if (createOk) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), cookies.toString().replace("[", "").replace("]", ""),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    private void writeCookieSession(String sessionCookie) {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "sessionCookie");
        boolean createOk = file.getParentFile().mkdirs();
        if (createOk) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), sessionCookie.replace("[", "").replace("]", ""), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
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

    private void writeXtoken(String accessToken) {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "xtoken");
        boolean createOk = file.getParentFile().mkdirs();
        if (createOk) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), accessToken, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    public @Nullable String readXtoken() {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "xtoken");
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

    private void writeMusicToken(String musicToken) {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "musicToken");
        boolean createOk = file.getParentFile().mkdirs();
        if (createOk) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), musicToken, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    public @Nullable String readMusicToken() {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "musicToken");
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
