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

package org.openhab.voice.yandexstation.internal;

public enum YandexVoices {
    YANDEX_VOICE_RU_ALENA("ru-RU", "русский", "alena", "Алёна", "female"),
    YANDEX_VOICE_RU_ANTON("ru-RU", "русский", "anton_samokhvalov", "Антон Самохвалов", "male"),
    YANDEX_VOICE_RU_VOICESEARCH("ru-RU", "русский", "voicesearch", "голосовой помошник", "male"),
    YANDEX_VOICE_RU_JANE("ru-RU", "русский", "jane", "Джейн", "female"),
    YANDEX_VOICE_RU_ERKANYAVAS("ru-RU", "русский", "erkanyavas", "Ерканявас", "male"),
    YANDEX_VOICE_RU_ERMIL("ru-RU", "русский", "ermil", "Ермил", "male"),
    YANDEX_VOICE_RU_ERMIL2("ru-RU", "русский", "ermil_with_tuning", "Ермил 2", "male"),
    YANDEX_VOICE_RU_ERMILOV("ru-RU", "русский", "ermilov", "Ермилов", "male"),
    YANDEX_VOICE_RU_ZHENYA("ru-RU", "русский", "zhenya", "Женя", "male"),
    YANDEX_VOICE_RU_ZAHAR("ru-RU", "русский", "zahar", "Захар", "male"),
    YANDEX_VOICE_RU_ZOMBY("ru-RU", "русский", "zombie", "зомби", "male"),
    YANDEX_VOICE_RU_KOSTYA("ru-RU", "русский", "kostya", "Костя", "male"),
    YANDEX_VOICE_RU_KOLYA("ru-RU", "русский", "kolya", "Коля", "male"),
    YANDEX_VOICE_RU_LEVITAN("ru-RU", "русский", "levitan", "Левитан", "male"),
    YANDEX_VOICE_RU_MADIRUS("ru-RU", "русский", "madirus", "Мадирус", "male"),
    YANDEX_VOICE_RU_NASTYA("ru-RU", "русский", "nastya", "Настя", "female"),
    YANDEX_VOICE_RU_NICK("ru-RU", "русский", "nick", "Ник", "female"),
    YANDEX_VOICE_RU_OKSANA("ru-RU", "русский", "oksana ", "Оксана", "female"),
    YANDEX_VOICE_RU_OMAZH("ru-RU", "русский", "omazh", "Omazh", "male"),
    YANDEX_VOICE_RU_ROBOT("ru-RU", "русский", "robot", "робот", "male"),
    YANDEX_VOICE_RU_SASHA("ru-RU", "русский", "sasha", "Саша", "male"),
    YANDEX_VOICE_RU_SILAERKAN("ru-RU", "русский", "silaerkan", "Силаеркан", "male"),
    YANDEX_VOICE_RU_SMOKY("ru-RU", "русский", "smoky", "Смоки", "male"),
    YANDEX_VOICE_RU_TANYA("ru-RU", "русский", "tanya", "Таня", "female"),
    YANDEX_VOICE_RU_TANYA2("ru-RU", "русский", "tatyana_abramova", "Татьяна Абрамова", "female"),
    YANDEX_VOICE_RU_FILIPP("ru-RU", "русский", "filipp", "Филипп", "male"),
    YANDEX_VOICE_RU_DUDE("ru-RU", "русский", "dude", "Чувак", "male"),
    YANDEX_VOICE_RU_ALYSS("ru-RU", "русский", "alyss", "Элис", "female"),
    ;

    private String locale;
    private String lang;
    private String voice;
    private String label;
    private String gender;

    YandexVoices(String locale, String lang, String voice, String label, String gender) {
        this.locale = locale;
        this.lang = lang;
        this.voice = voice;
        this.gender = gender;
        this.label = label;
    }

    public String getLocale() {
        return locale;
    }

    public String getLang() {
        return lang;
    }

    public String getVoice() {
        return voice;
    }

    public String getLabel() {
        return label;
    }

    public String getGender() {
        return gender;
    }
}
