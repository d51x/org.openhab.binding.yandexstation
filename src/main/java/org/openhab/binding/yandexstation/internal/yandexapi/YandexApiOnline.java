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
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
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
import org.eclipse.jetty.util.HttpCookieStore;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APICloudDevicesResponse;
import org.openhab.binding.yandexstation.internal.yandexapi.response.APIScenarioResponse;
import org.openhab.binding.yandexstation.internal.yandexapi.response.ApiResponse;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * The {@link YandexApiOnline} is describing implementaion of api interface.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class YandexApiOnline implements YandexApi {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final HttpClient httpClient;
    public static final String YANDEX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
    public static final String API_URL = "https://passport.yandex.ru/";
    private final CookieManager cookieManager;
    private volatile CookieStore cookieStore = new HttpCookieStore();

    public YandexApiOnline(HttpClient httpClient) {
        this.httpClient = httpClient;
        cookieManager = newCookieManager();
        cookieStore = getCookies(cookieManager.getCookieStore());
        this.httpClient.setCookieStore(cookieStore);
    }

    @Override
    public void update() {
    }

    @Override
    public void initialize() throws ApiException {
    }

    @Override
    public ApiResponse sendGetRequest(String path, @Nullable String params, @Nullable String token)
            throws ApiException {
        ApiResponse result = new ApiResponse();
        Request request;
        httpClient.setConnectTimeout(60 * 1000);
        if (params != null) {
            request = httpClient.newRequest(path + params);
        } else {
            request = httpClient.newRequest(path);
        }
        setHeaders(request, token);
        request.header(HttpHeader.CONTENT_TYPE, "text/html");
        request.header("charset", "utf-8");
        request.method(HttpMethod.GET);
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                result.response = contentResponse.getContentAsString();
                writeCookie(cookieStore);
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
    public ApiResponse sendGetRequest(String path, String token) {
        return new ApiResponse();
    }

    @Override
    public ApiResponse sendPostRequest(String path, String data, String token) throws ApiException {
        String errorReason = "";
        ApiResponse result = new ApiResponse();
        httpClient.setConnectTimeout(60 * 1000);
        Request request = httpClient.newRequest(path);
        request.timeout(60, TimeUnit.SECONDS);
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.header("charset", "utf-8");
        request.content(new StringContentProvider(data), "application/x-www-form-urlencoded");
        if (!token.isEmpty()) {
            request.header(HttpHeader.COOKIE, token);
        }
        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                result.response = contentResponse.getContentAsString();
                writeCookie(cookieStore);
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

    private ApiResponse sendPostRequestForToken(String path, String data) throws ApiException {
        String errorReason = "";
        if (cookieStore.getCookies().stream().anyMatch((session -> session.getName().equals("Session_id")))) {
            ApiResponse result = new ApiResponse();
            httpClient.setConnectTimeout(60 * 1000);
            Request request = httpClient.newRequest(path);
            request.timeout(60, TimeUnit.SECONDS);
            request.method(HttpMethod.POST);
            request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.header("charset", "utf-8");
            request.header("ya-client-host", "passport.yandex.ru");
            request.header("ya-client-cookie", "Session_id=" + cookieStore.getCookies().stream()
                    .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getValue());
            request.content(new StringContentProvider(data), "application/x-www-form-urlencoded");

            try {
                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                    result.response = contentResponse.getContentAsString();
                    JsonObject response = JsonParser.parseString(result.response).getAsJsonObject();
                    if (response.has("status") && response.get("status").getAsString().equals("error")) {
                        errorReason = String.format("Yandex API request failed with %s",
                                response.get("errors").getAsJsonArray().get(0).getAsString());
                        throw new ApiException(errorReason);
                    }
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

    public boolean getToken(String username, String password, String cookies) throws ApiException {
        if (!cookies.isEmpty()) {
            writeCookie(cookies);
            cookieStore = getCookies(cookieManager.getCookieStore());
        }
        if (cookieStore.getCookies().stream().noneMatch((session -> session.getName().equals("Session_id")))) {
            boolean capcha = false;
            String csrf_token = "";
            sendGetRequest(
                    "https://passport.yandex.ru/auth/welcome?retpath=https%3A%2F%2Fpassport.yandex.ru%2F&noreturn=1",
                    null, null);
            ApiResponse csrfTokenRequest;
            csrfTokenRequest = sendGetRequest(API_URL + "am?", "app_platform=android", null);
            String title = csrfTokenRequest.response.substring(csrfTokenRequest.response.indexOf("<title"),
                    csrfTokenRequest.response.indexOf("</title>"));
            if (title.contains("Ой!")) {
                if (readCaptchaCookie() != null) {
                    capcha = true;
                    csrfTokenRequest = sendGetRequest(API_URL + "am?", "app_platform=android", readCaptchaCookie());
                    if (csrfTokenRequest.response.substring(csrfTokenRequest.response.indexOf("<title"),
                            csrfTokenRequest.response.indexOf("</title>")).contains("Ой!"))
                        throw new ApiException("Please login via browser and copy cookie to passportCookie.json");
                } else {
                    throw new ApiException(
                            "Capcha requred. Please login via browser and copy cookie to passportCookie.json or fill captchaProtect file from browser");
                }
            }
            // logger.debug("{}", title);
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
            ApiResponse trackIdRequest;
            if (capcha) {
                trackIdRequest = sendPostRequest(API_URL + "registration-validations/auth/multi_step/start",
                        "csrf_token=" + csrf_token + "&login=" + username, Objects.requireNonNull(readCaptchaCookie()));
            } else {
                trackIdRequest = sendPostRequest(API_URL + "registration-validations/auth/multi_step/start",
                        "csrf_token=" + csrf_token + "&login=" + username, "");
            }
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
            ApiResponse passwordCheck;
            if (capcha) {
                passwordCheck = sendPostRequest(API_URL + "registration-validations/auth/multi_step/commit_password",
                        "csrf_token=" + csrf_token + "&track_id=" + trackId + "&password=" + password,
                        Objects.requireNonNull(readCaptchaCookie()));
            } else {
                passwordCheck = sendPostRequest(API_URL + "registration-validations/auth/multi_step/commit_password",
                        "csrf_token=" + csrf_token + "&track_id=" + trackId + "&password=" + password, "");
            }
            if (JsonParser.parseString(passwordCheck.response).getAsJsonObject().has("status")) {
                if (JsonParser.parseString(passwordCheck.response).getAsJsonObject().get("status").getAsString()
                        .equals("ok")) {
                    if (cookieStore.getCookies().stream()
                            .anyMatch((session -> session.getName().equals("Session_id")))) {
                        writeCookieSession(cookieStore);
                    }
                } else {
                    if (JsonParser.parseString(passwordCheck.response).getAsJsonObject().has("errors")) {
                        throw new ApiException(JsonParser.parseString(passwordCheck.response).getAsJsonObject()
                                .get("errors").getAsJsonArray().toString());
                    } else
                        throw new ApiException("Error sending password");
                }
            } else
                throw new ApiException("Password error");
            if (cookieStore.getCookies().stream().anyMatch((session -> session.getName().equals("Session_id")))) {
                ApiResponse getUserToken = sendPostRequestForToken(
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
                ApiResponse getMusicToken = sendPostRequestForToken("https://oauth.mobile.yandex.net/1/token",
                        "client_id=23cabbbdc6cd418abb4b39c32c41195d&client_secret=53bc75238f0c4d08a118e51fe9203300&grant_type=x-token&access_token="
                                + token.get("access_token").getAsString());
                JsonObject getMusicTokenJson = JsonParser.parseString(getMusicToken.response).getAsJsonObject();
                if (getMusicTokenJson.has("access_token")) {
                    writeMusicToken(getMusicTokenJson.get("access_token").getAsString());
                }

            }
        } else {
            if (readXtoken() == null) {
                ApiResponse getXToken = sendPostRequestForToken(
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

            ApiResponse getMusicToken = sendPostRequestForToken("https://oauth.mobile.yandex.net/1/token",
                    "client_id=23cabbbdc6cd418abb4b39c32c41195d&client_secret=53bc75238f0c4d08a118e51fe9203300&grant_type=x-token&access_token="
                            + readXtoken());
            JsonObject getMusicTokenJson = JsonParser.parseString(getMusicToken.response).getAsJsonObject();
            if (getMusicTokenJson.has("access_token")) {
                writeMusicToken(getMusicTokenJson.get("access_token").getAsString());
            }
        }
        return true;
    }

    public APICloudDevicesResponse getDevicesList() throws ApiException {
        ApiResponse response = sendGetRequest("https://iot.quasar.yandex.ru/m/v3/user/devices", null,
                "Session_id=" + cookieStore.getCookies().stream()
                        .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getValue());
        Gson gson = new Gson();
        APICloudDevicesResponse resp = gson.fromJson(response.response, APICloudDevicesResponse.class);
        return Objects.requireNonNullElseGet(resp, APICloudDevicesResponse::new);
    }

    public String getWssUrl() throws ApiException {
        return getDevicesList().updates_url;
    }

    private void setHeaders(Request request, @Nullable String token) {
        request.timeout(60, TimeUnit.SECONDS);
        request.header(HttpHeader.USER_AGENT, YANDEX_USER_AGENT);
        request.header(HttpHeader.CONNECTION, "keep-alive");
        request.header(HttpHeader.ACCEPT, "*/*");
        request.header(HttpHeader.ACCEPT_ENCODING, "deflate");
        if (token != null) {
            request.header(HttpHeader.COOKIE, token);
        }
        request.followRedirects(true);
    }

    public @Nullable String readCaptchaCookie() {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "captchaProtect");
        if (!file.exists()) {
            boolean createOk = file.getParentFile().mkdirs();
            if (createOk) {
                logger.debug("Folders {} created", file.getAbsolutePath());
            }
            try {
                Files.writeString(file.toPath(), "", StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("Cannot write to file {}", file.getName());
            }
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

    private void writeCaptchaCookie(CookieStore cookieStore) {
        var ref = new Object() {
            String captchaCookieString = "";
        };
        cookieStore.getCookies().forEach(cookie -> ref.captchaCookieString = ref.captchaCookieString + cookie.getName()
                + "=" + cookie.getValue() + ";");
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "captchaProtect");
        boolean createOk = file.getParentFile().mkdirs();
        if (createOk) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), ref.captchaCookieString, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    private CookieStore getCookies(CookieStore cookieStore) {
        File file = new File(OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator
                + "passportCookie.json");
        List<YandexCookies> cookiesList = null;
        if (!file.exists()) {
            return cookieManager.getCookieStore();
        } else {
            try {
                JsonReader reader = new JsonReader(new FileReader(file));
                Type listType = new TypeToken<ArrayList<YandexCookies>>() {
                }.getType();
                cookiesList = new Gson().fromJson(reader, listType);
                reader.close();
            } catch (IOException ignored) {
            }
        }
        if (cookiesList != null) {
            cookiesList.forEach(cookie -> {
                HttpCookie httpCookie = new HttpCookie(cookie.name, cookie.value);
                httpCookie.setHttpOnly(cookie.httpOnly);
                httpCookie.setPath(cookie.path);
                httpCookie.setSecure(cookie.secure);
                httpCookie.setDomain(cookie.domain);
                if (cookie.domain.isEmpty()) {
                    cookieStore.add(null, httpCookie);
                } else {
                    cookieStore.add(URI.create(cookie.domain), httpCookie);
                }
            });
        }
        return cookieManager.getCookieStore();
    }

    private void writeCookie(CookieStore cookieStore) {
        List<YandexCookies> lislCookies = new ArrayList<>();
        cookieStore.getCookies().forEach(cookie -> {
            YandexCookies cookies = new YandexCookies();
            cookies.domain = cookie.getDomain();
            cookies.value = cookie.getValue();
            cookies.name = cookie.getName();
            cookies.path = cookie.getPath();
            cookies.httpOnly = cookie.isHttpOnly();
            cookies.secure = cookie.getSecure();
            lislCookies.add(cookies);
        });
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String json = gson.toJson(lislCookies);
        File file = new File(OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator
                + "passportCookie.json");
        boolean createOk = file.getParentFile().mkdirs();
        if (createOk) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    private void writeCookie(String cookieStore) {
        File file = new File(OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator
                + "passportCookie.json");
        boolean createOk = file.getParentFile().mkdirs();
        if (createOk) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), cookieStore, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    private void writeCookieSession(CookieStore cookieStore) {
        var ref = new Object() {
            String sessionCookie = "";
        };
        cookieStore.getCookies().forEach(cookie -> {
            if (cookie.getName().equals("Session_id") /* || cookie.getName().equals("yandexuid") */) {
                ref.sessionCookie = ref.sessionCookie + cookie.getName() + "=" + cookie.getValue();
            }
        });
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "sessionCookie");
        boolean createOk = file.getParentFile().mkdirs();
        if (createOk) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), ref.sessionCookie.strip(), StandardCharsets.UTF_8);
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

    public @Nullable String readCSRFToken(boolean update) {
        File file = new File(
                OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + "csrfToken");
        if (update) {
            boolean isDeleted = file.delete();
            logger.debug("File {} delete status: {}", file.getName(), isDeleted);
        }
        if (!file.exists()) {
            String[] parseToken = new String[0];
            try {
                ApiResponse response = sendGetRequest("https://yandex.ru/quasar/iot", null, null);
                String getCsrfTokenString = response.response.substring(response.response.indexOf("{\"csrfToken2\":\""),
                        response.response.indexOf("{\"csrfToken2\":\"") + response.response
                                .substring(response.response.indexOf("{\"csrfToken2\":\"")).indexOf("\",\"cspNonce\""));
                logger.debug("csrf_token2 {}", getCsrfTokenString);
                parseToken = getCsrfTokenString.split("\":\"");
                Files.writeString(file.toPath(), parseToken[1], StandardCharsets.UTF_8);
                logger.debug("csrfResponse {}", parseToken[1]);
            } catch (ApiException | IOException ignored) {
            }
            return parseToken[1];
        } else {
            List<String> lines = null;
            try {
                lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
            if (lines != null) {
                return lines.get(0);
            } else {
                String[] parseToken = new String[0];
                try {
                    ApiResponse response = sendGetRequest("https://yandex.ru/quasar/iot", null,
                            "Session_id=" + cookieStore.getCookies().stream()
                                    .filter((session -> session.getName().equals("Session_id"))).findFirst().get()
                                    .getValue());
                    String getCsrfTokenString = response.response
                            .substring(response.response.indexOf("{\"csrfToken2\":\""),
                                    response.response.indexOf("{\"csrfToken2\":\"") + response.response
                                            .substring(response.response.indexOf("{\"csrfToken2\":\""))
                                            .indexOf("\",\"cspNonce\""));
                    logger.debug("csrf_token2 {}", getCsrfTokenString);
                    parseToken = getCsrfTokenString.split("\":\"");
                    Files.writeString(file.toPath(), parseToken[1], StandardCharsets.UTF_8);
                    logger.debug("csrfResponse {}", parseToken[1]);
                } catch (ApiException | IOException ignored) {
                }
                return parseToken[1];
            }
        }
    }

    public APIScenarioResponse getScenarios() {
        try {
            ApiResponse response = sendGetRequest("https://iot.quasar.yandex.ru/m/user/scenarios", null,
                    "Session_id=" + cookieStore.getCookies().stream()
                            .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getValue());
            Gson gson = new Gson();
            APIScenarioResponse scenarioJson = gson.fromJson(response.response, APIScenarioResponse.class);
            logger.debug("Scenarios json is: {}", response.response);
            if (scenarioJson != null) {
                return scenarioJson;
            }

        } catch (ApiException ignored) {
        }
        return new APIScenarioResponse();
    }

    public ApiResponse sendPostJsonRequest(String path, String json, String ignoredS) throws ApiException {
        String errorReason;
        if (cookieStore.getCookies().stream().anyMatch((session -> session.getName().equals("Session_id")))) {
            var ref = new Object() {
                String cookiesAsString = "";
            };
            cookieStore.getCookies().forEach(cook -> {
                if (!cook.getName().equals("yandexuid")) {
                    ref.cookiesAsString = ref.cookiesAsString + cook.getName() + "=" + cook.getValue() + ";";
                }
            });
            ApiResponse result = new ApiResponse();
            httpClient.setConnectTimeout(60 * 1000);
            Request request = httpClient.newRequest(path);
            request.timeout(60, TimeUnit.SECONDS);
            request.method(HttpMethod.POST);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.header("charset", "utf-8");
            request.header(HttpHeader.COOKIE, cookieStore.getCookies().stream()
                    .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getName()
                    + "=" + cookieStore.getCookies().stream()
                            .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getValue()
                    + ";"
                    + cookieStore.getCookies().stream().filter((session -> session.getName().equals("yandexuid")))
                            .findFirst().get().getName()
                    + "=" + cookieStore.getCookies().stream().filter((session -> session.getName().equals("yandexuid")))
                            .findFirst().get().getValue());
            logger.debug("csrf is: {}", Objects.requireNonNull(readCSRFToken(false)).strip());
            request.header("x-csrf-token", Objects.requireNonNull(readCSRFToken(false)).strip());
            request.content(new StringContentProvider(json), "application/json");

            try {
                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                    result.response = contentResponse.getContentAsString();
                    return result;
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    throw new ApiException(errorReason);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | ApiException e) {
                logger.error("ERROR {}", e.getMessage());
            }
        }
        return new ApiResponse();
    }

    public ApiResponse sendPutJsonRequest(String path, String json, String updateFlag) {
        if (!updateFlag.isEmpty()) {
            boolean update = true;
            readCSRFToken(update);
        }
        String errorReason;
        if (cookieStore.getCookies().stream().anyMatch((session -> session.getName().equals("Session_id")))) {
            ApiResponse result = new ApiResponse();
            httpClient.setConnectTimeout(60 * 1000);
            Request request = httpClient.newRequest(path);
            request.timeout(60, TimeUnit.SECONDS);
            request.method(HttpMethod.PUT);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.header("charset", "utf-8");
            logger.debug("csrf is: {}", Objects.requireNonNull(readCSRFToken(false)).strip());
            request.header("x-csrf-token", Objects.requireNonNull(readCSRFToken(false)).strip());
            request.content(new StringContentProvider(json), "application/json");
            request.header(HttpHeader.COOKIE, cookieStore.getCookies().stream()
                    .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getName()
                    + "=" + cookieStore.getCookies().stream()
                            .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getValue()
                    + ";"
                    + cookieStore.getCookies().stream().filter((session -> session.getName().equals("yandexuid")))
                            .findFirst().get().getName()
                    + "=" + cookieStore.getCookies().stream().filter((session -> session.getName().equals("yandexuid")))
                            .findFirst().get().getValue());
            try {
                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                    result.response = contentResponse.getContentAsString();
                    return result;
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    throw new ApiException(errorReason);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | ApiException e) {
                logger.error("ERROR {}", e.getMessage());
            }
        }
        return new ApiResponse();
    }

    public ApiResponse sendDeleteJsonRequest(String s) {
        logger.info("deleting {}", s);
        String errorReason;
        if (cookieStore.getCookies().stream().anyMatch((session -> session.getName().equals("Session_id")))) {
            var ref = new Object() {
                String cookiesAsString = "";
            };
            cookieStore.getCookies().forEach(cook -> {
                if (!cook.getName().equals("yandexuid")) {
                    ref.cookiesAsString = ref.cookiesAsString + cook.getName() + "=" + cook.getValue() + ";";
                }
            });
            ApiResponse result = new ApiResponse();
            httpClient.setConnectTimeout(60 * 1000);
            Request request = httpClient.newRequest(s);
            request.timeout(60, TimeUnit.SECONDS);
            request.method(HttpMethod.DELETE);
            request.header("charset", "utf-8");
            request.header(HttpHeader.COOKIE, cookieStore.getCookies().stream()
                    .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getName()
                    + "=" + cookieStore.getCookies().stream()
                            .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getValue()
                    + ";"
                    + cookieStore.getCookies().stream().filter((session -> session.getName().equals("yandexuid")))
                            .findFirst().get().getName()
                    + "=" + cookieStore.getCookies().stream().filter((session -> session.getName().equals("yandexuid")))
                            .findFirst().get().getValue());
            logger.debug("csrf is: {}", Objects.requireNonNull(readCSRFToken(false)).strip());
            request.header("x-csrf-token", Objects.requireNonNull(readCSRFToken(false)).strip());

            try {
                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200 || result.httpCode >= 400 && result.httpCode < 500) {
                    result.response = contentResponse.getContentAsString();
                    return result;
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    throw new ApiException(errorReason);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | ApiException e) {
                logger.error("ERROR {}", e.getMessage());
            }
        }
        return new ApiResponse();
    }

    private CookieManager newCookieManager() {
        return new CookieManager(getCookieStore(), CookiePolicy.ACCEPT_ALL);
    }

    /**
     * @return the cookie store associated with this instance
     */
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public ApiResponse sendPostRequest(String path, Fields fields, String token) {
        return new ApiResponse();
    }

    class YandexCookies {
        String domain = "";
        boolean httpOnly;
        String name = "";
        String path = "";
        boolean secure;
        String value = "";
    }
}
