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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link APICloudDevicesResponse} is describing implementaion of api interface.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class APICloudDevicesResponse {
    @Nullable
    String status;
    @Nullable
    Households[] households = new Households[0];
    @Nullable
    String updates_url;

    public APICloudDevicesResponse() {
    }

    public class Households {
        @Nullable
        String id;
        @Nullable
        String name;
        @Nullable
        Rooms[] rooms = new Rooms[0];
    }

    public class Rooms {
        @Nullable
        String id;
        @Nullable
        String name;
        Items[] items = new Items[0];
    }

    public class Items {
        @Nullable
        String id;
        @Nullable
        String name;
        @Nullable
        String type;
        Capabilities[] capabilities = new Capabilities[0];
    }

    public class Capabilities {
        @Nullable
        String type;
        @Nullable
        State state;
    }

    public class State {
        @Nullable
        String instance;
        @Nullable
        Object value;
    }
}
