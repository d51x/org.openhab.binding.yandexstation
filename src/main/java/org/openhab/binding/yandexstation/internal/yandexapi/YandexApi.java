/*
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information.
 *
 * This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.yandexstation.internal.yandexapi;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.Fields;

/**
 * The {@link YandexApi} is describing api interface.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public interface YandexApi {
    void update() throws ApiException;

    void initialize() throws ApiException;

    ApiResponse sendGetRequest(String path, @NonNull String params, String token) throws ApiException;

    ApiResponse sendGetRequest(String path, String token) throws ApiException;

    ApiResponse sendPostRequest(String path, String data, String token) throws ApiException;

    ApiResponse sendPostRequest(String path, Fields fields, String token) throws ApiException;
}
