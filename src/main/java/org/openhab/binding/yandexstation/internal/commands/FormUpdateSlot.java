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

/**
 * The {@link FormUpdateSlot} is responsible for Form Update action, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class FormUpdateSlot {
    private String type;
    private String name;
    private String value;

    /**
     * Instantiates a new Form update slot.
     */
    public FormUpdateSlot() {
        this.type = "string";
        this.name = "request";
        this.value = null;
    }

    /**
     * Instantiates a new Form update slot.
     *
     * @param value the value
     */
    public FormUpdateSlot(String value) {
        this.type = "string";
        this.name = "request";
        this.value = value;
    }

    /**
     * Instantiates a new Form update slot.
     *
     * @param type  the type
     * @param name  the name
     * @param value the value
     */
    public FormUpdateSlot(String type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }
}
