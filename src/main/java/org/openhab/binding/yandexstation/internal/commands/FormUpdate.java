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

import java.util.ArrayList;

/**
 * The {@link FormUpdate} is responsible for Form Update action, which are
 * sent to one of the channels.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class FormUpdate {
    private String name;
    private ArrayList<FormUpdateSlot> slots = new ArrayList<>();

    public FormUpdate() {
        this.name = "personal_assistant.scenarios.repeat_after_me";
    }

    public void setSlots(ArrayList<FormUpdateSlot> slots) {
        this.slots = slots;
    }

    public void addSlot(FormUpdateSlot slot) {
        this.slots.add(slot);
    }
}
