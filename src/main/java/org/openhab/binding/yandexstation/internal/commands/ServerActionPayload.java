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
package org.openhab.binding.yandexstation.internal.commands;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ServerActionPayload} is describing server action payload entity
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class ServerActionPayload {

    @SerializedName("form_update")
    private FormUpdate formUpdate;
    private Boolean resubmit;

    /**
     * Instantiates a new Server action payload.
     *
     * @param formUpdate the form update
     */
    public ServerActionPayload(FormUpdate formUpdate) {
        this.formUpdate = formUpdate;
        this.resubmit = null;
    }

    /**
     * Instantiates a new Server action payload.
     *
     * @param formUpdate the form update
     * @param resubmit   the resubmit
     */
    public ServerActionPayload(FormUpdate formUpdate, Boolean resubmit) {
        this.formUpdate = formUpdate;
        this.resubmit = resubmit;
    }
}
