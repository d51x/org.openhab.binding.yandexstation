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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YandexApiFactory} is describing api factory.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@Component(service = YandexApiFactory.class)
@NonNullByDefault
public class YandexApiFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HttpClient httpClient;

    /**
     * Instantiates a new Yandex api factory.
     *
     * @param httpClientFactory the http client factory
     */
    @Activate
    public YandexApiFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    /**
     * Gets api.
     *
     * @return the api
     * @throws ApiException the api exception
     */
    public YandexApi getApi() throws ApiException {
        return new YandexApiImpl(httpClient);
    }

    public YandexApi getTokenApi(String bridgeID) throws ApiException {
        return new QuasarApi(httpClient, bridgeID);
    }
}
