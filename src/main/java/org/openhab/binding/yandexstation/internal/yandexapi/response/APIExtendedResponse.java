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
package org.openhab.binding.yandexstation.internal.yandexapi.response;

import java.net.HttpCookie;
import java.util.List;

/**
 * The {@link APIExtendedResponse} is describing api common response.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class APIExtendedResponse extends ApiResponse {
    public List<HttpCookie> cookies;
}
