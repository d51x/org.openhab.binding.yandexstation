package org.openhab.binding.yandexstation.internal.yandexapi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.Fields;

@NonNullByDefault

public interface YandexApi {
    void update() throws ApiException;
    void initialize() throws ApiException;
    ApiResponse sendGetRequest(String path, String params, String token) throws ApiException;
    ApiResponse sendPostRequest(String path, String data, String token) throws ApiException;
    ApiResponse sendPostRequest(String path, Fields fields, String token) throws ApiException;

}
