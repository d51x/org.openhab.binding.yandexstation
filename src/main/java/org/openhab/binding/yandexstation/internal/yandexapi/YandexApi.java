/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.yandexstation.internal.yandexapi.response.ApiResponse;

/**
 * The {@link YandexApi} is describing api interface.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public interface YandexApi {
    /**
     * Update.
     *
     * @throws ApiException the api exception
     */
    void update() throws ApiException;

    /**
     * Initialize.
     *
     * @throws ApiException the api exception
     */
    void initialize() throws ApiException;

    /**
     * Send get request api response.
     *
     * @param path   the path
     * @param params the params
     * @param token  the token
     * @return the api response
     * @throws ApiException the api exception
     */
    ApiResponse sendGetRequest(String path, @NonNull String params, String token) throws ApiException;

    /**
     * Send get request api response.
     *
     * @param path  the path
     * @param token the token
     * @return the api response
     * @throws ApiException the api exception
     */
    ApiResponse sendGetRequest(String path, String token) throws ApiException;

    /**
     * Send post request api response.
     *
     * @param path  the path
     * @param data  the data
     * @param token the token
     * @return the api response
     * @throws ApiException the api exception
     */
    ApiResponse sendPostRequest(String path, String data, String token) throws ApiException;

    /**
     * Send post request api response.
     *
     * @param path   the path
     * @param fields the fields
     * @param token  the token
     * @return the api response
     * @throws ApiException the api exception
     */
    ApiResponse sendPostRequest(String path, Fields fields, String token) throws ApiException;
}
