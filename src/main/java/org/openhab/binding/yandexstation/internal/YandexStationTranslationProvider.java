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
package org.openhab.binding.yandexstation.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;

/**
 * The type Yandex station translation provider.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
@NonNullByDefault
public class YandexStationTranslationProvider {

    private final Bundle bundle;
    private final @Nullable TranslationProvider i18nProvider;
    private final @Nullable LocaleProvider localeProvider;
    @Nullable
    private final Locale locale;

    /**
     * Instantiates a new Yandex station translation provider.
     *
     * @param i18nProvider the 18 n provider
     * @param localeProvider the locale provider
     * @param locale the locale
     */
    @Activate
    public YandexStationTranslationProvider(@Nullable TranslationProvider i18nProvider,
            @Nullable LocaleProvider localeProvider, @Nullable Locale locale) {
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.locale = locale;
        this.bundle = FrameworkUtil.getBundle(this.getClass());
    }

    /**
     * Gets text.
     *
     * @param key the key
     * @param defaultText the default text
     * @param arguments the arguments
     * @return the text
     */
    public String getText(String key, String defaultText, @Nullable Object... arguments) {
        String text = i18nProvider.getText(bundle, key, defaultText,
                locale != null ? locale : localeProvider.getLocale(), arguments);
        if (text == null) {
            return defaultText;
        }
        return text;
    }
}
