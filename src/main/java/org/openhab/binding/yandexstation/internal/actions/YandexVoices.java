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
package org.openhab.binding.yandexstation.internal.actions;

/**
 * The enum Yandex voices.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 *
 */
public enum YandexVoices {
    /**
     * Yandex voice ru alena yandex voices.
     */
    YANDEX_VOICE_RU_ALENA("ru-RU", "русский", "alena", "Алёна", "female"),
    /**
     * Yandex voice ru anton yandex voices.
     */
    YANDEX_VOICE_RU_ANTON("ru-RU", "русский", "anton_samokhvalov", "Антон Самохвалов", "male"),
    /**
     * Yandex voice ru voicesearch yandex voices.
     */
    YANDEX_VOICE_RU_VOICESEARCH("ru-RU", "русский", "voicesearch", "голосовой помошник", "male"),
    /**
     * Yandex voice ru jane yandex voices.
     */
    YANDEX_VOICE_RU_JANE("ru-RU", "русский", "jane", "Джейн", "female"),
    /**
     * Yandex voice ru erkanyavas yandex voices.
     */
    YANDEX_VOICE_RU_ERKANYAVAS("ru-RU", "русский", "erkanyavas", "Ерканявас", "male"),
    /**
     * Yandex voice ru ermil yandex voices.
     */
    YANDEX_VOICE_RU_ERMIL("ru-RU", "русский", "ermil", "Ермил", "male"),
    /**
     * Yandex voice ru ermil 2 yandex voices.
     */
    YANDEX_VOICE_RU_ERMIL2("ru-RU", "русский", "ermil_with_tuning", "Ермил 2", "male"),
    /**
     * Yandex voice ru ermilov yandex voices.
     */
    YANDEX_VOICE_RU_ERMILOV("ru-RU", "русский", "ermilov", "Ермилов", "male"),
    /**
     * Yandex voice ru zhenya yandex voices.
     */
    YANDEX_VOICE_RU_ZHENYA("ru-RU", "русский", "zhenya", "Женя", "male"),
    /**
     * Yandex voice ru zahar yandex voices.
     */
    YANDEX_VOICE_RU_ZAHAR("ru-RU", "русский", "zahar", "Захар", "male"),
    /**
     * Yandex voice ru zomby yandex voices.
     */
    YANDEX_VOICE_RU_ZOMBY("ru-RU", "русский", "zombie", "зомби", "male"),
    /**
     * Yandex voice ru kostya yandex voices.
     */
    YANDEX_VOICE_RU_KOSTYA("ru-RU", "русский", "kostya", "Костя", "male"),
    /**
     * Yandex voice ru kolya yandex voices.
     */
    YANDEX_VOICE_RU_KOLYA("ru-RU", "русский", "kolya", "Коля", "male"),
    /**
     * Yandex voice ru levitan yandex voices.
     */
    YANDEX_VOICE_RU_LEVITAN("ru-RU", "русский", "levitan", "Левитан", "male"),
    /**
     * Yandex voice ru madirus yandex voices.
     */
    YANDEX_VOICE_RU_MADIRUS("ru-RU", "русский", "madirus", "Мадирус", "male"),
    /**
     * Yandex voice ru nastya yandex voices.
     */
    YANDEX_VOICE_RU_NASTYA("ru-RU", "русский", "nastya", "Настя", "female"),
    /**
     * Yandex voice ru nick yandex voices.
     */
    YANDEX_VOICE_RU_NICK("ru-RU", "русский", "nick", "Ник", "female"),
    /**
     * Yandex voice ru oksana yandex voices.
     */
    YANDEX_VOICE_RU_OKSANA("ru-RU", "русский", "oksana ", "Оксана", "female"),
    /**
     * Yandex voice ru omazh yandex voices.
     */
    YANDEX_VOICE_RU_OMAZH("ru-RU", "русский", "omazh", "Omazh", "male"),
    /**
     * Yandex voice ru robot yandex voices.
     */
    YANDEX_VOICE_RU_ROBOT("ru-RU", "русский", "robot", "робот", "male"),
    /**
     * Yandex voice ru sasha yandex voices.
     */
    YANDEX_VOICE_RU_SASHA("ru-RU", "русский", "sasha", "Саша", "male"),
    /**
     * Yandex voice ru silaerkan yandex voices.
     */
    YANDEX_VOICE_RU_SILAERKAN("ru-RU", "русский", "silaerkan", "Силаеркан", "male"),
    /**
     * Yandex voice ru smoky yandex voices.
     */
    YANDEX_VOICE_RU_SMOKY("ru-RU", "русский", "smoky", "Смоки", "male"),
    /**
     * Yandex voice ru tanya yandex voices.
     */
    YANDEX_VOICE_RU_TANYA("ru-RU", "русский", "tanya", "Таня", "female"),
    /**
     * Yandex voice ru tanya 2 yandex voices.
     */
    YANDEX_VOICE_RU_TANYA2("ru-RU", "русский", "tatyana_abramova", "Татьяна Абрамова", "female"),
    /**
     * Yandex voice ru filipp yandex voices.
     */
    YANDEX_VOICE_RU_FILIPP("ru-RU", "русский", "filipp", "Филипп", "male"),
    /**
     * Yandex voice ru dude yandex voices.
     */
    YANDEX_VOICE_RU_DUDE("ru-RU", "русский", "dude", "Чувак", "male"),
    /**
     * Yandex voice ru alyss yandex voices.
     */
    YANDEX_VOICE_RU_ALYSS("ru-RU", "русский", "alyss", "Элис", "female"),;

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

    /**
     * Gets locale.
     *
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets lang.
     *
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Gets voice.
     *
     * @return the voice
     */
    public String getVoice() {
        return voice;
    }

    /**
     * Gets label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets gender.
     *
     * @return the gender
     */
    public String getGender() {
        return gender;
    }
}
