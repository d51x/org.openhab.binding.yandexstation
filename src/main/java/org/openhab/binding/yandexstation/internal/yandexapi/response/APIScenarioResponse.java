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
 */
package org.openhab.binding.yandexstation.internal.yandexapi.response;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link APIScenarioResponse} is describing api common success response.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class APIScenarioResponse {
    public String status;
    public Scenarios[] scenarios;

    public APIScenarioResponse() {
        status = "";
        scenarios = new Scenarios[0];
    }

    public class Scenarios {
        public String id = "";
        public String name = "";
        public Triggers[] triggers = new Triggers[0];
    }

    public class Triggers {
        public String type = "";
        public String value = "";
    }
}
