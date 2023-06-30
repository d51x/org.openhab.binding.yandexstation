package org.openhab.binding.yandexstation.internal.yandexapi;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@NonNullByDefault
public class YandexApiImpl  implements YandexApi {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final HttpClient httpClient;

    public static final String YANDEX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";

    public static final String API_URL = "https://quasar.yandex.net/glagol";
    public static final String API_PATH_DEVICE_TOKEN = "/token";
    public static final String API_PATH_DEVICE_LIST = "/device_list";

    public YandexApiImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void update() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public ApiResponse sendGetRequest(String path, @NonNull String params, String token) throws ApiException {
        String pathWithParams = path;
        if (!params.isEmpty()) {
            pathWithParams += "?" + params;
        }
        return sendGetRequest(pathWithParams,token);
    }

    @Override
    public ApiResponse sendGetRequest(String path, String token) throws ApiException {
        String url = API_URL + path;
        ApiResponse result = new ApiResponse();
        httpClient.setConnectTimeout(60*1000);

        Request request = httpClient.newRequest(url);
        setHeaders(request, token);
        request.method(HttpMethod.GET);
        String errorReason;
        try {
            ContentResponse contentResponse = request.send();
            result.httpCode = contentResponse.getStatus();
            if (result.httpCode == 200 ||
                    result.httpCode >= 400 && result.httpCode < 500) {
                result.response = contentResponse.getContentAsString();
                return result;
            } else {
                errorReason = String.format("Yandex API request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getReason());
            }
        } catch (TimeoutException e) {
            errorReason = "TimeoutException: Yandex API was not reachable on your network";
        } catch (ExecutionException e) {
            errorReason = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorReason = String.format("InterruptedException: %s", e.getMessage());
        }
        throw new ApiException(errorReason);
    }

    @Override
    public ApiResponse sendPostRequest(String path, String data, String token) {

        return new ApiResponse();
    }

    @Override
    public ApiResponse sendPostRequest(String path, Fields fields, String token) {

        return new ApiResponse();
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
        //request.header(HttpHeader.CACHE_CONTROL, "no-cache");
        if (token != null) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + token);
        }
        request.followRedirects(true);
    }

    public String getDeviceToken(String yandexToken, String device_id, String platform) throws ApiException {
        logger.info("Try to get device token for {}", platform);
        StringBuilder params = new StringBuilder();
        params.append("device_id=");
        params.append(device_id);
        params.append("&platform=");
        params.append(platform);
        try {
            ApiResponse response = sendGetRequest(API_PATH_DEVICE_TOKEN, params.toString(), yandexToken);
            if (response.httpCode == 200) {
                ApiTokenResponse tokenResponse = new Gson().fromJson(response.response, ApiTokenResponse.class);
                logger.info("Device token is: {}", tokenResponse.token);
                return tokenResponse.token;
            } else {
                throw new ApiException(String.format("YandexApi get token error: httpCode = %d", response.httpCode));
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("JsonSyntaxException:{}", e);
        }
    }

    public List<ApiDeviceResponse> getDevices(@NonNull String yandexToken) throws ApiException {
        logger.info("Try to get device list");
        try {
            ApiResponse response = sendGetRequest(API_PATH_DEVICE_LIST, yandexToken);
            if (response.httpCode == 200) {
                JsonObject json = JsonParser.parseString(response.response).getAsJsonObject();
                JsonArray deviceList = json.getAsJsonArray("devices");
                Type listType = new TypeToken<ArrayList<ApiDeviceResponse>>(){}.getType();
                List<ApiDeviceResponse> devices = new Gson().fromJson(deviceList, listType);
                logger.info("Device list is: {}", devices);
                if (!devices.isEmpty()) {

                    return devices;
                } else {
                    return new ArrayList<>();
                }
            } else {
                throw new ApiException(String.format("YandexApi get device list error: httpCode = %d", response.httpCode));
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("JsonSyntaxException:{}", e);
        }
    }

    public ApiDeviceResponse findDevice(@NonNull String deviceId, @NonNull String yandexToken)  throws ApiException {
        List<ApiDeviceResponse> devices = getDevices(yandexToken);
        return devices.stream().filter(dev -> dev.id.equals(deviceId) ).findFirst().orElse(new ApiDeviceResponse());
    }
}