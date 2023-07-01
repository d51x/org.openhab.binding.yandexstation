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
package org.openhab.binding.yandexstation.internal.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link YandexStationResponse} is describing api response with station state entity
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationResponse {
    @SerializedName("id")
    private String responseId;
    private String requestId;
    private String softwareVersion;
    private Long requestSentTime;
    private Long processingTime;
    private String status;
    private YandexStationState state;

    private YandexStationResponseExtra extra;

    @SerializedName("supported_features")
    private List<String> supportedFeatures = new ArrayList<>();

    @SerializedName("unsupported_features")
    private List<String> unSupportedFeatures = new ArrayList<>();

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public Long getRequestSentTime() {
        return requestSentTime;
    }

    public void setRequestSentTime(Long requestSentTime) {
        this.requestSentTime = requestSentTime;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public YandexStationState getState() {
        return state;
    }

    public void setState(YandexStationState state) {
        this.state = state;
    }

    public YandexStationResponseExtra getExtra() {
        return extra;
    }

    public void setExtra(YandexStationResponseExtra extra) {
        this.extra = extra;
    }

    public List<String> getSupportedFeatures() {
        return supportedFeatures;
    }

    public void setSupportedFeatures(List<String> supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    public List<String> getUnSupportedFeatures() {
        return unSupportedFeatures;
    }

    public void setUnSupportedFeatures(List<String> unSupportedFeatures) {
        this.unSupportedFeatures = unSupportedFeatures;
    }

    public class YandexStationResponseExtra {
        public String appState;
        public String environmentState;
        public String watchedVideoState;
        public String softwareVersion;
    }
}
