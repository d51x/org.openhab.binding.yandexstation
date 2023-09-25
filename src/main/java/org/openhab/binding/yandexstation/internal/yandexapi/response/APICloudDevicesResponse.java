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

/**
 * The {@link APICloudDevicesResponse} is describing implementaion of api interface.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class APICloudDevicesResponse {
    public String status = "";
    Households[] households = new Households[0];
    public String updates_url = "";

    public class Households {
        String id = "";
        String name = "";
        Rooms[] rooms = new Rooms[0];
    }

    public class Rooms {
        String id = "";
        String name = "";
        Items[] items = new Items[0];
    }

    public class Items {
        String id = "";
        String name = "";
        String type = "";
        Capabilities[] capabilities = new Capabilities[0];
    }

    public class Capabilities {
        String type = "";
        State state = new State();
    }

    public class State {
        String instance = "";
        Object value = "";
    }
}
