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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.yandexstation.internal.YandexStationHandler;

/**
 * The {@link ApiException} is responsible for handling api exceptions.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class ApiException extends Exception {
    private static final long serialVersionUID = -1748312966538510299L;
    public ApiException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Api exception.
     *
     * @param message the message
     * @param e       the e
     */
    public ApiException(String message, Throwable e) {
        super(message, e);
    }
}
