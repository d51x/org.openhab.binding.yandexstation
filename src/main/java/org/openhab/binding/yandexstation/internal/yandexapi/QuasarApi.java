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
 *
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
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
import com.google.gson.JsonParseException;
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

    public static final String FILE_SCENARIOS = "scenarios";
    public static final String FILE_CAPTCHA = "captchaProtect";
    public static final String FILE_PASSPORT_COOKIE = "passportCookie.json";
    public static final String FILE_SESSION_COOKIE = "sessionCookie";
    public static final String FILE_X_TOKEN = "xtoken";
    public static final String FILE_MUSIC_TOKEN = "musicToken";
    public static final String FILE_CSRF_TOKEN = "csrfToken";

    private final CookieManager cookieManager;
    private volatile CookieStore cookieStore = new HttpCookieStore();
    private String bridgeID = "";

    private Gson gson = new Gson();
    private YandexSession yaSession = new YandexSession();

    private CookieUtils cookieUtils = new CookieUtils();

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

    public ApiResponse getTrackIdRequest(String path, String data, String xToken) throws ApiException {
        HttpFields headers = new HttpFields();

        if (yaSession.csrfToken.isEmpty()) {
            yaSession.csrfToken = readCSRFToken(false).strip();
        }
        logger.trace("csrf is: {}", yaSession.csrfToken);

        headers.add("x-csrf-token", yaSession.csrfToken);
        headers.add(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        headers.add("charset", "utf-8");
        headers.add("Ya-Consumer-Authorization", "OAuth " + xToken);
        ContentProvider content = new StringContentProvider("application/x-www-form-urlencoded", data,
                StandardCharsets.UTF_8);
        return sendRequest(path, "", content, HttpMethod.POST, headers);
    }

    private ApiResponse getXTokenRequest(String path, String data) throws ApiException {
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            HttpFields headers = new HttpFields();

            // if (yaSession.csrfToken.isEmpty()) {
            // yaSession.csrfToken = readCSRFToken(true).strip();
            // }
            // logger.trace("csrf is: {}", yaSession.csrfToken);

            // headers.add("x-csrf-token", yaSession.csrfToken);
            headers.add(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
            headers.add("charset", "utf-8");
            headers.add("ya-client-host", "passport.yandex.ru");

            String sessionId = cookieUtils.extractSessionIdFromCookie(cookieStore);
            headers.add("ya-client-cookie", "Session_id=" + sessionId);

            ContentProvider content = new StringContentProvider("application/x-www-form-urlencoded", data,
                    StandardCharsets.UTF_8);

            ApiResponse result = sendRequest(path, "", content, HttpMethod.POST, headers);
            JsonObject jsonResponse = JsonParser.parseString(result.response).getAsJsonObject();
            if (jsonResponse.has("status") && jsonResponse.get("status").getAsString().equals("error")) {
                String errorReason = String.format("Yandex API request failed with %s",
                        jsonResponse.get("errors").getAsJsonArray().get(0).getAsString());
                throw new ApiException(errorReason);
            }
            return result;
        }
        return new ApiResponse();
    }

    private String getCsrfToken() throws ApiException {
        ApiResponse csrfTokenRequest = sendGetRequest(API_CSRF_TOKEN_URL, "app_platform=android", "");
        String csrfToken = cookieUtils.extractCSRFToken(csrfTokenRequest.response);
        logger.debug("csrf_token {}", csrfToken);

        if (csrfToken.isEmpty()) {
            logger.error("csrf_token not found");
            throw new ApiException("XXXX");
        } else if (csrfToken.equals("captcha")) {
            String cookie = readCaptchaCookie();
            if (!cookie.isBlank()) {
                csrfTokenRequest = sendGetRequest(API_CSRF_TOKEN_URL, "app_platform=android", cookie);
                csrfToken = cookieUtils.extractCSRFToken(csrfTokenRequest.response);
                logger.debug("csrf_token {}", csrfToken);

                if (csrfToken.isEmpty()) {
                    logger.error("csrf_token not found");
                    throw new ApiException("XXXX");
                } else if (csrfToken.equals("captcha")) {
                    throw new ApiException("Please login via browser and copy cookie to passportCookie.json");
                } else {
                    throw new ApiException(
                            "Capcha requred. Please login via browser and copy cookie to passportCookie.json or fill captchaProtect file from browser");
                }
            } else {
                throw new ApiException("Please login via browser and copy cookie to passportCookie.json");
            }
        }

        return csrfToken;
    }

    private String getTrackId(String login, String csrfToken) throws ApiException {
        String trackId = "";
        String data = "csrf_token=" + csrfToken + "&login=" + login;
        String cookie = readCaptchaCookie();
        ApiResponse trackIdRequest = sendPostRequest(API_REGISTRATION_START_URL, data,
                "application/x-www-form-urlencoded", cookie);

        JsonObject trackIdObj = JsonParser.parseString(trackIdRequest.response).getAsJsonObject();
        if (trackIdObj.has("status") && trackIdObj.get("status").getAsString().equals("ok")
                && trackIdObj.has("can_authorize")
                && Boolean.TRUE.equals(trackIdObj.get("can_authorize").getAsBoolean()) && trackIdObj.has("track_id")) {
            trackId = trackIdObj.get("track_id").getAsString();
            logger.debug("track_id {}", trackId);
        } else {
            throw new ApiException("Cannot fetch track_id");
        }
        return trackId;
    }

    private String getXToken(String additionalParams) throws ApiException {
        String xToken = "";
        String data = USER_TOKEN_CLIENT_ID + additionalParams;
        ApiResponse xTokenResponse = getXTokenRequest(API_PROXY_PASSPORT_URL, data);

        xToken = cookieUtils.extractAccessToken(xTokenResponse.response);
        if (!xToken.isBlank()) {
            writeXtoken(xToken);
        } else {
            logger.debug("Cannot fetch xToken");
        }
        return xToken;
    }

    private String getMusicToken(String xToken) throws ApiException {
        String musicToken = "";
        String data = MUSIC_TOKEN_CLIENT_ID + "&access_token=" + xToken;
        ApiResponse getMusicToken = getXTokenRequest(OAUTH_MOBILE_URL, data);

        musicToken = cookieUtils.extractAccessToken(getMusicToken.response);
        if (!musicToken.isBlank()) {
            writeMusicToken(musicToken);
        } else {
            logger.debug("Cannot fetch musicToken");
        }
        return musicToken;
    }

    private void passwordCheck(String csrfToken, String trackId, String password) throws ApiException {
        String data = "csrf_token=" + csrfToken + "&track_id=" + trackId + "&password=" + password;
        String cookie = ""; // readCaptchaCookie();
        ApiResponse response = sendPostRequest(API_REGISTRATION_COMMIT_URL, data, "application/x-www-form-urlencoded",
                cookie);

        JsonObject result = JsonParser.parseString(response.response).getAsJsonObject();
        if (result.has("status") && result.get("status").getAsString().equals("ok")) {
            if (cookieUtils.isCookieHasSessionId(cookieStore)) {
                writeCookieSession(cookieStore);
            }
        } else if (result.has("errors")) {
            throw new ApiException(result.get("errors").getAsJsonArray().toString());
        } else {
            throw new ApiException("Error sending password");
        }
    }

    public YandexSession createSession(String username, String password, String cookies) throws ApiException {
        yaSession = new YandexSession(username, password);

        if (!cookies.isEmpty()) {
            writeCookie(cookies);
            cookieStore = getCookies(cookieManager.getCookieStore());
        } else {
            deleteCookieFile();
        }

        if (cookieUtils.isCookieNoSessionId(cookieStore)) {

            sendGetRequest(API_AUTH_WELCOME_URL, "", ""); // why?

            yaSession.csrfToken = getCsrfToken();
            yaSession.trackId = getTrackId(username, yaSession.csrfToken);

            passwordCheck(yaSession.csrfToken, yaSession.trackId, password);

            if (cookieUtils.isCookieHasSessionId(cookieStore)) {
                yaSession.xToken = getXToken("&track_id=" + yaSession.trackId);
                yaSession.musicToken = getMusicToken(yaSession.xToken);
            }
        } else {
            yaSession.xToken = readXtoken();
            if (yaSession.xToken.isEmpty()) {
                yaSession.xToken = getXToken("");
            }
            yaSession.musicToken = getMusicToken(yaSession.xToken);
        }
        return yaSession;
    }

    public boolean refreshCookie() throws ApiException {
        yaSession.xToken = readXtoken();
        if (yaSession.xToken.isBlank()) {
            logger.error("refreshCookie: xToken is empty");
            return false;
        }
        String passportHost = "";

        String data = "type=x-token&retpath=https://www.yandex.ru";
        ApiResponse trackIdResponse = getTrackIdRequest(API_PROXY_AUTH_X_TOKEN_URL, data, yaSession.xToken);

        JsonObject trackIdObj = JsonParser.parseString(trackIdResponse.response).getAsJsonObject();
        if (trackIdObj.has("status") && trackIdObj.get("status").getAsString().equals("ok")
                && trackIdObj.has("track_id") && trackIdObj.has("passport_host")) {
            yaSession.trackId = trackIdObj.get("track_id").getAsString();
            passportHost = trackIdObj.get("passport_host").getAsString();
            logger.debug("track_id {}", yaSession.trackId);

            ApiResponse response = sendGetRequest(passportHost + "/auth/session/", "?track_id=" + yaSession.trackId,
                    "");
            if (response.httpCode != 200) {
                logger.error("Cannot refresh cookie");
                throw new ApiException("Cannot refresh cookie");
            }
        } else {
            logger.error("Cannot refresh cookie");
            throw new ApiException("Cannot fetch track_id");
        }

        yaSession.csrfToken = readCSRFToken(true);
        return true;
    }

    public APICloudDevicesResponse getDevicesList() throws ApiException {
        String sessionId = cookieUtils.extractSessionIdFromCookie(cookieStore);
        ApiResponse response = sendGetRequest(DEVICES_URL, "", "Session_id=" + sessionId);
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

    public File getFile(String name) {
        return new File(OpenHAB.getUserDataFolder() + File.separator + "YandexStation" + File.separator + bridgeID + "_"
                + name);
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
        } catch (JsonParseException e) {
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

    public void deleteScenariosFile() {
        File file = getFile(FILE_SCENARIOS);
        file.delete();
    }

    private String refreshCSRFToken(String cookie) {
        String csrfToken = "";
        // HttpCookie session = extractParamFromCookie("Session_id", cookieStore);
        // HttpCookie yandexUid = extractParamFromCookie("yandexuid", cookieStore);
        // String cookies = session.getName() + "=" + session.getValue() + ";" + yandexUid.getName() + "="
        // + yandexUid.getValue();

        try {
            HttpFields headers = new HttpFields();
            if (!cookie.isBlank()) {
                headers.add(HttpHeader.COOKIE, cookie);
            }
            headers.add(HttpHeader.CONTENT_TYPE, "text/html");
            headers.add("charset", "utf-8");

            ApiResponse response = sendGetRequest(QUASAR_IOT_URL, "", headers);
            csrfToken = cookieUtils.extractCSRFToken2(response.response);
            logger.debug("csrf_token2 {}", csrfToken);
        } catch (ApiException ignored) {
        }
        return csrfToken;
    }

    public String readCSRFToken(boolean update) {
        String csrfToken = "";
        File file = getFile(FILE_CSRF_TOKEN);
        if (update) {
            boolean isDeleted = file.delete();
            logger.debug("File {} delete status: {}", file.getName(), isDeleted);
        }
        try {
            String cookie = "Session_id=" + cookieUtils.extractSessionIdFromCookie(cookieStore);
            if (!file.exists()) {
                csrfToken = refreshCSRFToken(cookie);
                Files.writeString(file.toPath(), csrfToken, StandardCharsets.UTF_8);
            } else {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    csrfToken = lines.get(0);
                } else {

                    csrfToken = refreshCSRFToken(cookie);
                    Files.writeString(file.toPath(), csrfToken, StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {

        }
        return csrfToken;
    }

    public APIScenarioResponse getScenarios() {
        try {
            String cookie = cookieUtils.extractSessionIdFromCookie(cookieStore);
            ApiResponse response = sendGetRequest(SCENARIOUS_URL, "", "Session_id=" + cookie);
            logger.debug("Scenarios json is: {}", response.response);
            APIScenarioResponse resp = gson.fromJson(response.response, APIScenarioResponse.class);
            return resp != null ? resp : new APIScenarioResponse();

        } catch (ApiException | NullPointerException ignored) {
        }
        return new APIScenarioResponse();
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

    public boolean createScenario(String scenario) throws ApiException {
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            HttpCookie session = cookieUtils.extractParamFromCookie("Session_id", cookieStore);
            HttpCookie yandexuid = cookieUtils.extractParamFromCookie("yandexuid", cookieStore);
            String cookie = session.getName() + "=" + session.getValue() + ";" + yandexuid.getName() + "="
                    + yandexuid.getValue();

            ApiResponse response = sendPostRequest(SCENARIOUS_URL, scenario, "application/json", cookie);
            logger.debug("response script creation: {}", response.response);
            return response.httpCode == 200;
        }
        return false;
    }

    public boolean updateScenario(String scenarioId, String scenario) throws ApiException {
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            ApiResponse response = sendPutRequest(SCENARIOUS_URL + "/" + scenarioId, scenario, "application/json");
            logger.debug("response script update: {}", response.response);
            return response.httpCode == 200;
        }
        return false;
    }

    public boolean deleteScenario(String scenarioId) throws ApiException {
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            ApiResponse response = sendDeleteRequest(SCENARIOUS_URL + "/" + scenarioId);
            logger.debug("response script delete: {}", response.response);
            return response.httpCode == 200;
        }
        return false;
    }

    // **************************************************************************
    // ******** Request functions ***********************************************
    // **************************************************************************
    private ApiResponse sendRequest(String path, String params, @Nullable ContentProvider content, HttpMethod method,
            HttpFields headers) throws ApiException {
        logger.debug("send {}-request: {}", method, path);

        if (HttpMethod.GET.equals(method)) {
            httpClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        }

        ApiResponse result = new ApiResponse();
        ;
        String errorReason = "";
        Integer retry = 3;
        while (retry > 0) {
            Request request = httpClient.newRequest(path + (params.isEmpty() ? "" : params));
            // request.getHeaders().put(HttpHeader.USER_AGENT, YANDEX_USER_AGENT);

            if (headers.size() > 0) {
                request.getHeaders().addAll(headers);
            }

            request.method(method);

            try {
                if (content != null) {
                    request.content(content);
                }

                ContentResponse contentResponse = request.send();
                result.httpCode = contentResponse.getStatus();
                if (result.httpCode == 200) {
                    result.response = contentResponse.getContentAsString();
                    writeCookie(cookieStore);
                    return result;
                } else if (result.httpCode == 401) {
                    errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                    result.response = contentResponse.getReason();
                    logger.error("sendRequest {}: {}", method, errorReason);
                    refreshCookie();
                    retry = 0;
                } else if (result.httpCode == 403) {
                    errorReason = contentResponse.getStatus() + " " + contentResponse.getReason();
                    result.response = contentResponse.getReason();
                    yaSession.csrfToken = readCSRFToken(true);
                    headers.put("x-csrf-token", yaSession.csrfToken);
                    retry--;
                    logger.error("sendRequest {}: {}", method, errorReason);
                } else {
                    errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                            contentResponse.getReason());
                    logger.error("sendRequest {}: {}", method, errorReason);
                    result.response = contentResponse.getReason();
                    retry = 0;
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                retry = 0;
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement s : e.getStackTrace()) {
                    sb.append(s.toString()).append("\n");
                }
                logger.error("sendRequest ERROR: {}. Stacktrace: \n{}", e.getMessage(), sb);
            }
        }
        throw new ApiException(result, errorReason);
    }

    public ApiResponse sendGetRequest(String path, String params, String cookie) throws ApiException {
        HttpFields headers = new HttpFields();
        if (!cookie.isEmpty()) {
            headers.add(HttpHeader.COOKIE, cookie);
        }
        return sendRequest(path, params, null, HttpMethod.GET, headers);
    }

    public ApiResponse sendGetRequest(String path, String params, HttpFields headers) throws ApiException {
        return sendRequest(path, params, null, HttpMethod.GET, headers);
    }

    @Override
    public ApiResponse sendGetRequest(String path, String cookie) throws ApiException {
        return sendGetRequest(path, "", cookie);
    }

    @Override
    public ApiResponse sendPostRequest(String path, HttpFields fields, String token) {
        return new ApiResponse();
    }

    @Override
    public ApiResponse sendPostRequest(String path, String data, String token) throws ApiException {
        return new ApiResponse();
    }

    public ApiResponse sendPostRequest(String path, String data, String contentType, String cookie)
            throws ApiException {
        HttpFields headers = new HttpFields();

        if (yaSession.csrfToken.isEmpty()) {
            yaSession.csrfToken = readCSRFToken(false).strip();
        }
        logger.trace("csrf is: {}", yaSession.csrfToken);

        headers.add("x-csrf-token", yaSession.csrfToken);
        headers.add(HttpHeader.CONTENT_TYPE, contentType);
        headers.add("charset", "utf-8");

        if (!cookie.isEmpty()) {
            headers.add(HttpHeader.COOKIE, cookie);
        }
        ContentProvider content = new StringContentProvider(contentType, data, StandardCharsets.UTF_8);
        return sendRequest(path, "", content, HttpMethod.POST, headers);
    }

    public ApiResponse sendPutRequest(String path, String data, String contentType) throws ApiException {
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            HttpFields headers = new HttpFields();
            headers.add("charset", "utf-8");
            headers.add(HttpHeader.CONTENT_TYPE, contentType);

            HttpCookie session = cookieUtils.extractParamFromCookie("Session_id", cookieStore);
            HttpCookie yandexUid = cookieUtils.extractParamFromCookie("yandexuid", cookieStore);

            headers.add(HttpHeader.COOKIE, session.getName() + "=" + session.getValue() + ";" + yandexUid.getName()
                    + "=" + yandexUid.getValue());

            if (yaSession.csrfToken.isEmpty()) {
                yaSession.csrfToken = readCSRFToken(false).strip();
            }
            logger.trace("csrf is: {}", yaSession.csrfToken);
            headers.add("x-csrf-token", yaSession.csrfToken);

            ContentProvider content = new StringContentProvider(contentType, data, StandardCharsets.UTF_8);
            return sendRequest(path, "", content, HttpMethod.PUT, headers);
        }
        return new ApiResponse();
    }

    public ApiResponse sendDeleteRequest(String path) throws ApiException {
        if (cookieUtils.isCookieHasSessionId(cookieStore)) {
            HttpFields headers = new HttpFields();
            headers.add("charset", "utf-8");

            HttpCookie session = cookieUtils.extractParamFromCookie("Session_id", cookieStore);
            HttpCookie yandexUid = cookieUtils.extractParamFromCookie("yandexuid", cookieStore);

            headers.add(HttpHeader.COOKIE, session.getName() + "=" + session.getValue() + ";" + yandexUid.getName()
                    + "=" + yandexUid.getValue());

            if (yaSession.csrfToken.isEmpty()) {
                yaSession.csrfToken = readCSRFToken(false).strip();
            }
            logger.trace("csrf is: {}", yaSession.csrfToken);
            headers.add("x-csrf-token", yaSession.csrfToken);

            return sendRequest(path, "", null, HttpMethod.DELETE, headers);
        }
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
