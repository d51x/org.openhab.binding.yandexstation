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
 *
 */

package org.openhab.binding.yandexstation.internal.yandexapi;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.Arrays;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link CookieUtils} is describing CookieUtils.
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class CookieUtils {
    public CookieUtils() {
    }

    public HttpCookie extractParamFromCookie(String name, CookieStore cookieStore) {
        return cookieStore.getCookies().stream().filter(session -> session.getName().equals(name)).findFirst()
                .orElseGet(() -> new HttpCookie(name, ""));
    }

    public String extractSessionIdFromCookie(CookieStore cookieStore) {
        return extractParamFromCookie("Session_id", cookieStore).getValue();
    }

    public boolean isCookieHasSessionId(CookieStore store) {
        return store.getCookies().stream().anyMatch(session -> session.getName().equals("Session_id"));
    }

    public boolean isCookieNoSessionId(CookieStore store) {
        return store.getCookies().stream().noneMatch(session -> session.getName().equals("Session_id"));
    }

    public String extractCSRFToken(String body) {
        String token = "";
        String title = body.substring(body.indexOf("<title"), body.indexOf("</title>"));
        if (title.contains("Ой") || title.contains("Капча")) {
            token = "captcha";
        } else {
            String data = body.substring(body.indexOf("<body"), body.indexOf("</body>"));
            token = data.substring(data.indexOf("name=\"csrf_token\""), data.indexOf("name=\"csrf_token\"")
                    + data.substring(data.indexOf("name=\"csrf_token\"")).indexOf("\"/>"));
            String[] parseToken = token.replaceAll("\"", "").split("=");
            if (Arrays.asList(parseToken).contains("csrf_token value")) {
                token = parseToken[2];
            }
        }
        return token;
    }

    public String extractCSRFToken2(String data) {
        String token = data.substring(data.indexOf("{\"csrfToken2\":\""), data.indexOf("{\"csrfToken2\":\"")
                + data.substring(data.indexOf("{\"csrfToken2\":\"")).indexOf("\",\"cspNonce\""));
        String[] parseToken = token.split("\":\"");
        return parseToken[1];
    }

    public String extractAccessToken(String json) {
        String token = "";
        JsonObject tokenJson = JsonParser.parseString(json).getAsJsonObject();
        if (tokenJson.has("status") && tokenJson.get("status").getAsString().equals("ok")
                || tokenJson.has("access_token")) {
            token = tokenJson.get("access_token").getAsString();
        }
        return token;
    }
}
