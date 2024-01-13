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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * The {@link QuasarApi} is describing implementaion of api interface.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class QuasarApi implements YandexApi {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final HttpClient httpClient;
    public static final String YANDEX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
    public static final String USER_TOKEN_CLIENT_ID = "client_id=c0ebe342af7d48fbbbfcf2d2eedb8f9e&client_secret=ad0a908f0aa341a182a37ecd75bc319e";
    public static final String MUSIC_TOKEN_CLIENT_ID = "client_id=23cabbbdc6cd418abb4b39c32c41195d&client_secret=53bc75238f0c4d08a118e51fe9203300&grant_type=x-token";
    public static final String API_PROXY_PASSPORT_URL = "https://mobileproxy.passport.yandex.net/1/bundle/oauth/token_by_sessionid";
    public static final String API_PROXY_AUTH_X_TOKEN_URL = "https://mobileproxy.passport.yandex.net/1/bundle/auth/x_token";
    public static final String OAUTH_MOBILE_URL = "https://oauth.mobile.yandex.net/1/token";
    public static final String API_PASSPORT_URL = "https://passport.yandex.ru";
    public static final String API_REGISTRATION_START_URL = API_PASSPORT_URL
            + "/registration-validations/auth/multi_step/start";
    public static final String API_REGISTRATION_COMMIT_URL = API_PASSPORT_URL
            + "/registration-validations/auth/multi_step/commit_password";
    public static final String API_CSRF_TOKEN_URL = API_PASSPORT_URL + "/am?";
    public static final String API_AUTH_WELCOME_URL = API_PASSPORT_URL
            + "/auth/welcome?retpath=https%3A%2F%2Fpassport.yandex.ru%2F&noreturn=1";
    public static final String SCENARIOUS_URL = "https://iot.quasar.yandex.ru/m/user/scenarios";
    public static final String DEVICES_URL = "https://iot.quasar.yandex.ru/m/v3/user/devices";
    public static final String QUASAR_IOT_URL = "https://yandex.ru/quasar/iot";

    public static final String FILE_CAPTCHA = "captchaProtect";
    public static final String FILE_PASSPORT_COOKIE = "passportCookie.json";
    public static final String FILE_SESSION_COOKIE = "sessionCookie";
    public static final String FILE_X_TOKEN = "xtoken";
    public static final String FILE_MUSIC_TOKEN = "musicToken";
    public static final String FILE_CSRF_TOKEN = "csrfToken";

    private final CookieManager cookieManager;
    private volatile CookieStore cookieStore = new HttpCookieStore();
    private String bridgeID = "";

    private volatile YandexSession yaSession = new YandexSession();
    private CookieUtils cookieUtils = new CookieUtils();
    private Gson gson = new Gson();

    public QuasarApi(HttpClient httpClient, String bridgeID) {
        this.httpClient = httpClient;
        this.httpClient.setConnectTimeout(60 * 1000);

        this.bridgeID = bridgeID;
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

    private File getFile(String name) {
        return new File(OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + bridgeID + "_"
                + name);
    }

    public ApiResponse sendGetRequestWithXToken(String path, @Nullable String params, @Nullable String token)
            throws ApiException {
        ApiResponse result = new ApiResponse();
        Request request;
        httpClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        if (params != null) {
            request = httpClient.newRequest(path + params);
        } else {
            request = httpClient.newRequest(path);
        }
        // setHeadersWithXToken(request, token);
        request.method(HttpMethod.GET);
        String errorReason = "";
        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 /* || result.httpCode >= 400 && result.httpCode < 500 */) {
                result.response = contentResponse.getContentAsString();
                writeCookie(cookieStore);
                return result;
            } else if (result.httpCode == 401) {
                errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                logger.error("sendGetRequest: {}", errorReason);
            } else {
                errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
                logger.error("sendGetRequest: {}", errorReason);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            // logger.error("ERROR {}", e.getMessage());
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement s : e.getStackTrace()) {
                sb.append(s.toString()).append("\n");
            }
            logger.error("sendGetRequest ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb.toString());
        }
        throw new ApiException(errorReason);
    }

    @Override
    public ApiResponse sendGetRequest(String path, @Nullable String params, @Nullable String token)
            throws ApiException {
        ApiResponse result = new ApiResponse();
        Request request;
        httpClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
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
            if (result.httpCode == 200 /* || result.httpCode >= 400 && result.httpCode < 500 */) {
                result.response = contentResponse.getContentAsString();
                writeCookie(cookieStore);
                return result;
            } else if (result.httpCode == 401) {
                errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                logger.error("sendGetRequest: {}", errorReason);
            } else {
                errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
                logger.error("sendGetRequest: {}", errorReason);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            // logger.error("ERROR {}", e.getMessage());
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement s : e.getStackTrace()) {
                sb.append(s.toString()).append("\n");
            }
            logger.error("sendGetRequest ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb.toString());
        }
        throw new ApiException(errorReason);
    }

    @Override
    public ApiResponse sendGetRequest(String path, String token) {
        return new ApiResponse();
    }

    public ApiResponse sendPostRequestWithXToken(String path, String data, String xToken) throws ApiException {
        String errorReason = "";
        ApiResponse result = new ApiResponse();
        Request request = httpClient.newRequest(path);
        request.timeout(60, TimeUnit.SECONDS);
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.header("charset", "utf-8");
        request.header("Ya-Consumer-Authorization", "OAuth " + xToken);
        request.content(new StringContentProvider(data), "application/x-www-form-urlencoded");
        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 /* || result.httpCode >= 400 && result.httpCode < 500 */) {
                result.response = contentResponse.getContentAsString();
                return result;
            } else if (result.httpCode == 401) {
                errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                logger.error("sendPostRequest: {}", errorReason);
            } else {
                errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
                logger.error("sendPostRequest: {}", errorReason);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            // logger.error("ERROR {}", e.getMessage());
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement s : e.getStackTrace()) {
                sb.append(s.toString()).append("\n");
            }
            logger.error("sendPostRequest ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb.toString());
        }
        throw new ApiException(errorReason);
    }

    @Override
    public ApiResponse sendPostRequest(String path, String data, String token) throws ApiException {
        String errorReason = "";
        ApiResponse result = new ApiResponse();
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
            if (result.httpCode == 200 /* || result.httpCode >= 400 && result.httpCode < 500 */) {
                result.response = contentResponse.getContentAsString();
                writeCookie(cookieStore);
                return result;
            } else if (result.httpCode == 401) {
                errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                logger.error("sendPostRequest: {}", errorReason);
            } else {
                errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
                logger.error("sendPostRequest: {}", errorReason);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            // logger.error("ERROR {}", e.getMessage());
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement s : e.getStackTrace()) {
                sb.append(s.toString()).append("\n");
            }
            logger.error("sendPostRequest ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb.toString());
        }
        throw new ApiException(errorReason);
    }

    private ApiResponse sendPostRequestForToken(String path, String data) throws ApiException {
        String errorReason = "";
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            ApiResponse result = new ApiResponse();
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
                if (result.httpCode == 200 /* || result.httpCode >= 400 && result.httpCode < 500 */) {
                    result.response = contentResponse.getContentAsString();
                    JsonObject response = JsonParser.parseString(result.response).getAsJsonObject();
                    if (response.has("status") && response.get("status").getAsString().equals("error")) {
                        errorReason = String.format("Yandex API request failed with %s",
                                response.get("errors").getAsJsonArray().get(0).getAsString());
                        throw new ApiException(errorReason);
                    }
                    return result;
                } else if (result.httpCode == 401) {
                    errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                    logger.error("sendPostRequestForToken: {}", errorReason);
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    logger.error("sendPostRequestForToken: {}", errorReason);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                // logger.error("ERROR {}", e.getMessage());
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement s : e.getStackTrace()) {
                    sb.append(s.toString()).append("\n");
                }
                logger.error("sendPostRequestForToken ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb.toString());
            }
        }

        throw new ApiException(errorReason);
    }

    public boolean getToken(String username, String password, String cookies) throws ApiException {
        if (!cookies.isEmpty()) {
            writeCookie(cookies);
            cookieStore = getCookies(cookieManager.getCookieStore());
        }
        if (cookieUtils.isCookieNoSessionId(cookieStore)) {
            boolean capcha = false;
            String csrf_token = "";
            sendGetRequest(API_AUTH_WELCOME_URL, null, null);
            ApiResponse csrfTokenRequest;
            csrfTokenRequest = sendGetRequest(API_CSRF_TOKEN_URL, "app_platform=android", null);
            String title = csrfTokenRequest.response.substring(csrfTokenRequest.response.indexOf("<title"),
                    csrfTokenRequest.response.indexOf("</title>"));
            if (title.contains("Ой!")) {
                if (readCaptchaCookie() != null) {
                    capcha = true;
                    csrfTokenRequest = sendGetRequest(API_CSRF_TOKEN_URL, "app_platform=android", readCaptchaCookie());
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
                trackIdRequest = sendPostRequest(API_REGISTRATION_START_URL,
                        "csrf_token=" + csrf_token + "&login=" + username, Objects.requireNonNull(readCaptchaCookie()));
            } else {
                trackIdRequest = sendPostRequest(API_REGISTRATION_START_URL,
                        "csrf_token=" + csrf_token + "&login=" + username, "");
            }
            JsonObject trackIdObj = JsonParser.parseString(trackIdRequest.response).getAsJsonObject();
            if (trackIdObj.has("status") && trackIdObj.get("status").getAsString().equals("ok")
                    && trackIdObj.has("can_authorize")
                    && Boolean.TRUE.equals(trackIdObj.get("can_authorize").getAsBoolean())
                    && trackIdObj.has("track_id")) {
                trackId = trackIdObj.get("track_id").getAsString();
                logger.debug("track_id {}", trackId);
            } else {
                throw new ApiException("Cannot fetch track_id");
            }
            ApiResponse passwordCheck;
            if (capcha) {
                passwordCheck = sendPostRequest(API_REGISTRATION_COMMIT_URL,
                        "csrf_token=" + csrf_token + "&track_id=" + trackId + "&password=" + password,
                        Objects.requireNonNull(readCaptchaCookie()));
            } else {
                passwordCheck = sendPostRequest(API_REGISTRATION_COMMIT_URL,
                        "csrf_token=" + csrf_token + "&track_id=" + trackId + "&password=" + password, "");
            }
            if (JsonParser.parseString(passwordCheck.response).getAsJsonObject().has("status")) {
                if (JsonParser.parseString(passwordCheck.response).getAsJsonObject().get("status").getAsString()
                        .equals("ok")) {
                    if (cookieUtils.isCookieHasSessionId(cookieStore)) {
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
            if (cookieUtils.isCookieHasSessionId(cookieStore)) {
                ApiResponse getUserToken = sendPostRequestForToken(API_PROXY_PASSPORT_URL,
                        USER_TOKEN_CLIENT_ID + "&track_id=" + trackId);
                logger.debug("Token resonse is {}", getUserToken.response);
                JsonObject token = JsonParser.parseString(getUserToken.response).getAsJsonObject();
                if (token.has("status")) {
                    if (token.get("status").getAsString().equals("ok")) {
                        writeXtoken(token.get("access_token").getAsString());
                    }
                }
                ApiResponse getMusicToken = sendPostRequestForToken(OAUTH_MOBILE_URL,
                        MUSIC_TOKEN_CLIENT_ID + "&access_token=" + token.get("access_token").getAsString());
                JsonObject getMusicTokenJson = JsonParser.parseString(getMusicToken.response).getAsJsonObject();
                if (getMusicTokenJson.has("access_token")) {
                    writeMusicToken(getMusicTokenJson.get("access_token").getAsString());
                }

            }
        } else {
            if (readXtoken().isBlank()) {
                ApiResponse getXToken = sendPostRequestForToken(API_PROXY_PASSPORT_URL, USER_TOKEN_CLIENT_ID);
                JsonObject token = JsonParser.parseString(getXToken.response).getAsJsonObject();
                if (token.has("status")) {
                    if (token.get("status").getAsString().equals("ok")) {
                        writeXtoken(token.get("access_token").getAsString());
                    } else {
                        logger.debug("Cannot fetch Xtoken");
                    }
                }
            }

            ApiResponse getMusicToken = sendPostRequestForToken(OAUTH_MOBILE_URL,
                    MUSIC_TOKEN_CLIENT_ID + "&access_token=" + readXtoken());
            JsonObject getMusicTokenJson = JsonParser.parseString(getMusicToken.response).getAsJsonObject();
            if (getMusicTokenJson.has("access_token")) {
                writeMusicToken(getMusicTokenJson.get("access_token").getAsString());
            }
        }
        return true;
    }

    public boolean refreshCookie() throws ApiException {
        String xToken = readXtoken();
        if (xToken.isBlank()) {
            logger.error("refreshCookie: xToken is empty");
            return false;
        }
        String trackId = "";
        String passportHost = "";

        String data = "type=x-token&retpath=https://www.yandex.ru";
        ApiResponse trackIdResponse = sendPostRequestWithXToken(API_PROXY_AUTH_X_TOKEN_URL, data, xToken);
        JsonObject trackIdObj = JsonParser.parseString(trackIdResponse.response).getAsJsonObject();
        if (trackIdObj.has("status") && trackIdObj.get("status").getAsString().equals("ok")
                && trackIdObj.has("track_id") && trackIdObj.has("passport_host")) {
            trackId = trackIdObj.get("track_id").getAsString();
            passportHost = trackIdObj.get("passport_host").getAsString();
            logger.debug("track_id {}", trackId);

            ApiResponse response = sendGetRequestWithXToken(passportHost + "/auth/session/", "?track_id=" + trackId,
                    xToken);
            if (response.httpCode != 200) {
                logger.error("Cannot refresh cookie");
                throw new ApiException("Cannot refresh cookie");
            }
        } else {
            logger.error("Cannot refresh cookie");
            throw new ApiException("Cannot fetch track_id");
        }
        return true;
    }

    public APICloudDevicesResponse getDevicesList() throws ApiException {
        ApiResponse response = sendGetRequest(DEVICES_URL, null, "Session_id=" + cookieStore.getCookies().stream()
                .filter((session -> session.getName().equals("Session_id"))).findFirst().get().getValue());
        Gson gson = new Gson();
        APICloudDevicesResponse resp = gson.fromJson(response.response, APICloudDevicesResponse.class);
        return Objects.requireNonNullElseGet(resp, APICloudDevicesResponse::new);
    }

    public String getWssUrl() throws ApiException {
        return getDevicesList().updates_url;
    }

    public Map<String, String> getDevices() throws ApiException {
        Map<String, String> yandexDevices = new HashMap<>();
        APICloudDevicesResponse devices = getDevicesList();
        for (APICloudDevicesResponse.Households house : devices.households) {
            for (APICloudDevicesResponse.Rooms room : house.rooms) {
                for (APICloudDevicesResponse.Items item : room.items) {
                    if (item.guasarInfo != null) {
                        logger.debug("station ID {}", item.guasarInfo.deviceId);
                        yandexDevices.put(item.id, item.guasarInfo.deviceId);
                    }
                }
            }
        }
        return yandexDevices;
    }

    private void setHeaders(Request request, @Nullable String token) {
        request.timeout(60, TimeUnit.SECONDS);

        if (token != null) {
            request.header(HttpHeader.COOKIE, token);
        }
        request.followRedirects(true);
    }

    private void setHeadersWithXToken(Request request, @Nullable String token) {
        request.timeout(60, TimeUnit.SECONDS);

        if (token != null) {
            request.header("Ya-Consumer-Authorization", "OAuth " + token);
        }
        request.followRedirects(false);
    }

    public String readCaptchaCookie() {
        String cookie = "";
        File file = getFile(FILE_CAPTCHA);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                Files.writeString(file.toPath(), "", StandardCharsets.UTF_8);
            } else {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    cookie = lines.get(0);
                }
            }
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
        return cookie;
    }

    private CookieStore getCookies(CookieStore cookieStore) {
        File file = getFile(FILE_PASSPORT_COOKIE);
        List<YandexCookies> cookiesList = null;
        try {
            if (!file.exists()) {
                return cookieManager.getCookieStore();
            } else {
                JsonReader reader = new JsonReader(new FileReader(file));
                Type listType = new TypeToken<ArrayList<YandexCookies>>() {
                }.getType();
                cookiesList = gson.fromJson(reader, listType);
                reader.close();
            }
        } catch (IOException e) {
            logger.error("Can't read passportCookie.json: {}", e.getMessage());
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
        File file = getFile(FILE_PASSPORT_COOKIE);

        if (file.getParentFile().mkdirs()) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    private void writeCookie(String cookieStore) {
        try {
            JsonArray testInput = JsonParser.parseString(cookieStore).getAsJsonArray();
            logger.debug("JSON Cookie from bridge input field: {}", testInput);
            if (testInput.isJsonArray()) {
                File file = getFile(FILE_PASSPORT_COOKIE);
                if (file.getParentFile().mkdirs()) {
                    logger.debug("Folders {} created", file.getAbsolutePath());
                }
                try {
                    Files.writeString(file.toPath(), cookieStore, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    logger.error("Cannot write to file {}", file.getName());
                }
            }
        } catch (Exception ignored) {
            logger.error("Input string is not JSON!");
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

        File file = getFile(FILE_SESSION_COOKIE);
        if (file.getParentFile().mkdirs()) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), ref.sessionCookie.strip(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    private void writeXtoken(String accessToken) {
        File file = getFile(FILE_X_TOKEN);
        if (file.getParentFile().mkdirs()) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }

        try {
            Files.writeString(file.toPath(), accessToken, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    public String readXtoken() {
        String token = "";
        File file = getFile(FILE_X_TOKEN);
        try {
            if (file.exists()) {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    token = lines.get(0);
                }
            }
        } catch (IOException e) {
            logger.error("Can't read xtoken file");
        }
        return token;
    }

    private void writeMusicToken(String musicToken) {
        File file = getFile(FILE_MUSIC_TOKEN);
        if (file.getParentFile().mkdirs()) {
            logger.debug("Folders {} created", file.getAbsolutePath());
        }
        try {
            Files.writeString(file.toPath(), musicToken, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Cannot write to file {}", file.getName());
        }
    }

    public String readMusicToken() {
        String token = "";
        File file = getFile(FILE_MUSIC_TOKEN);
        try {
            if (file.exists()) {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    token = lines.get(0);
                }
            }
        } catch (IOException e) {
            logger.error("Can't read musicToken file");
        }
        return token;
    }

    public void deleteCookieFile() {
        File file = getFile(FILE_PASSPORT_COOKIE);
        file.delete();
    }

    public void deleteCaptchaFile() {
        File file = getFile(FILE_CAPTCHA);
        file.delete();
    }

    public void deleteSessionFile() {
        File file = getFile(FILE_SESSION_COOKIE);
        file.delete();
    }

    public void deleteXTokenFile() {
        File file = getFile(FILE_X_TOKEN);
        file.delete();
    }

    public void deleteMusicTokenFile() {
        File file = getFile(FILE_MUSIC_TOKEN);
        file.delete();
    }

    public void deleteCsrfTokenFile() {
        File file = getFile(FILE_CSRF_TOKEN);
        file.delete();
    }

    public @Nullable String readCSRFToken(boolean update) {
        String csrfToken = "";
        File file = getFile(FILE_CSRF_TOKEN);
        if (update) {
            boolean isDeleted = file.delete();
            logger.debug("File {} delete status: {}", file.getName(), isDeleted);
        }
        try {
            if (!file.exists()) {
                ApiResponse response = sendGetRequest(QUASAR_IOT_URL, null, null);
                csrfToken = cookieUtils.extractCSRFToken2(response.response);
                ;
                logger.debug("csrf_token2 {}", csrfToken);
                Files.writeString(file.toPath(), csrfToken, StandardCharsets.UTF_8);
            } else {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    csrfToken = lines.get(0);
                } else {
                    String cookie = "Session_id=" + cookieUtils.extractSessionIdFromCookie(cookieStore);
                    ApiResponse response = sendGetRequest(QUASAR_IOT_URL, null, cookie);
                    csrfToken = cookieUtils.extractCSRFToken2(response.response);
                    ;
                    logger.debug("csrf_token2 {}", csrfToken);
                    Files.writeString(file.toPath(), csrfToken, StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {

        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return csrfToken;
    }

    public APIScenarioResponse getScenarios() {
        try {
            String cookie = "Session_id=" + cookieUtils.extractSessionIdFromCookie(cookieStore);
            ApiResponse response = sendGetRequest(SCENARIOUS_URL, null, cookie);
            logger.debug("Scenarios json is: {}", response.response);
            APIScenarioResponse resp = gson.fromJson(response.response, APIScenarioResponse.class);
            return resp != null ? resp : new APIScenarioResponse();
        } catch (ApiException ignored) {
        }
        return new APIScenarioResponse();
    }

    public ApiResponse sendPostJsonRequest(String path, String json, String ignoredS) throws ApiException {
        String errorReason;
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            ApiResponse result = new ApiResponse();
            Request request = httpClient.newRequest(path);
            request.timeout(60, TimeUnit.SECONDS);
            request.method(HttpMethod.POST);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.header("charset", "utf-8");

            HttpCookie session = cookieUtils.extractParamFromCookie("Session_id", cookieStore);
            HttpCookie yandexuid = cookieUtils.extractParamFromCookie("yandexuid", cookieStore);
            String cookie = session.getName() + "=" + session.getValue() + ";" + yandexuid.getName() + "="
                    + yandexuid.getValue();
            request.header(HttpHeader.COOKIE, cookie);

            String csrfToken = Objects.requireNonNull(readCSRFToken(false)).strip();
            logger.debug("csrf is: {}", csrfToken);
            request.header("x-csrf-token", csrfToken);
            request.content(new StringContentProvider(json), "application/json");

            try {
                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200 /* || result.httpCode >= 400 && result.httpCode < 500 */) {
                    result.response = contentResponse.getContentAsString();
                    return result;
                } else if (result.httpCode == 401) {
                    errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                    logger.error("sendPostJsonRequest: {}", errorReason);
                    throw new ApiException(errorReason);
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    logger.error("sendPostJsonRequest: {}", errorReason);
                    throw new ApiException(errorReason);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | ApiException e) {
                // logger.error("ERROR {}", e.getMessage());
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement s : e.getStackTrace()) {
                    sb.append(s.toString()).append("\n");
                }
                logger.error("sendPostJsonRequest ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb);
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
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            ApiResponse result = new ApiResponse();
            Request request = httpClient.newRequest(path);
            request.timeout(60, TimeUnit.SECONDS);
            request.method(HttpMethod.PUT);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.header("charset", "utf-8");

            String csrfToken = Objects.requireNonNull(readCSRFToken(false)).strip();
            logger.debug("csrf is: {}", csrfToken);
            request.header("x-csrf-token", csrfToken);

            request.content(new StringContentProvider(json), "application/json");

            HttpCookie session = cookieUtils.extractParamFromCookie("Session_id", cookieStore);
            HttpCookie yandexuid = cookieUtils.extractParamFromCookie("yandexuid", cookieStore);
            String cookie = session.getName() + "=" + session.getValue() + ";" + yandexuid.getName() + "="
                    + yandexuid.getValue();
            request.header(HttpHeader.COOKIE, cookie);

            try {
                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200 /* || result.httpCode >= 400 && result.httpCode < 500 */) {
                    result.response = contentResponse.getContentAsString();
                    return result;
                } else if (result.httpCode == 401) {
                    errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                    logger.error("sendPutJsonRequest: {}", errorReason);
                    throw new ApiException(errorReason);
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    logger.error("sendPutJsonRequest: {}", errorReason);
                    throw new ApiException(errorReason);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | ApiException e) {
                // logger.error("ERROR {}", e.getMessage());
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement s : e.getStackTrace()) {
                    sb.append(s.toString()).append("\n");
                }
                logger.error("sendPutJsonRequest ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb);
            }
        }
        return new ApiResponse();
    }

    public ApiResponse sendDeleteJsonRequest(String s) {
        logger.info("deleting {}", s);
        String errorReason;
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            ApiResponse result = new ApiResponse();
            Request request = httpClient.newRequest(s);
            request.timeout(60, TimeUnit.SECONDS);
            request.method(HttpMethod.DELETE);
            request.header("charset", "utf-8");

            HttpCookie session = cookieUtils.extractParamFromCookie("Session_id", cookieStore);
            HttpCookie yandexuid = cookieUtils.extractParamFromCookie("yandexuid", cookieStore);
            String cookie = session.getName() + "=" + session.getValue() + ";" + yandexuid.getName() + "="
                    + yandexuid.getValue();
            request.header(HttpHeader.COOKIE, cookie);

            String csrfToken = Objects.requireNonNull(readCSRFToken(false)).strip();
            logger.debug("csrf is: {}", csrfToken);
            request.header("x-csrf-token", csrfToken);

            try {
                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200 /* || result.httpCode >= 400 && result.httpCode < 500 */) {
                    result.response = contentResponse.getContentAsString();
                    return result;
                } else if (result.httpCode == 401) {
                    errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                    logger.error("sendDeleteJsonRequest: {}", errorReason);
                    throw new ApiException(errorReason);
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    logger.error("sendDeleteJsonRequest: {}", errorReason);
                    throw new ApiException(errorReason);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | ApiException e) {
                // logger.error("ERROR {}", e.getMessage());
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement s2 : e.getStackTrace()) {
                    sb.append(s2.toString()).append("\n");
                }
                logger.error("sendDeleteJsonRequest ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb);
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
